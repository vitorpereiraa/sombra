package com.github.vitorpereiraa.sombra.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class ReportingIT extends BaseIT {

    @Autowired
    private MeterRegistry meterRegistry;

    private ListAppender<ILoggingEvent> appender;
    private Logger reportingLogger;

    @BeforeEach
    void attachAppender() {
        var context = (LoggerContext) LoggerFactory.getILoggerFactory();
        reportingLogger = context.getLogger("com.github.vitorpereiraa.sombra.service.ExchangeLogger");
        reportingLogger.setLevel(Level.DEBUG);
        appender = new ListAppender<>();
        appender.setContext(context);
        appender.start();
        reportingLogger.addAppender(appender);
    }

    @AfterEach
    void detachAppender() {
        reportingLogger.detachAppender(appender);
        appender.stop();
    }

    @Test
    void shouldRecordMatchMetricsAndLog() {
        client.post()
                .uri("/echo")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"name\":\"test\",\"value\":1}")
                .exchange()
                .expectStatus()
                .isOk();

        await().atMost(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    var count = meterRegistry
                            .counter(
                                    "sombra.exchange.processed",
                                    "outcome", "match",
                                    "method", "POST",
                                    "status_class", "2xx")
                            .count();
                    assertThat(count).isGreaterThanOrEqualTo(1);
                });

        assertThat(appender.list).anyMatch(e -> {
            var kvs = keyValues(e);
            return Boolean.TRUE.equals(kvs.get("matched")) && "/echo".equals(kvs.get("request_path"));
        });
    }

    @Test
    void shouldRecordMismatchMetricsAndLogDiscrepancyDetails() {
        client.post()
                .uri("/echo/different")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"name\":\"original\",\"value\":1}")
                .exchange()
                .expectStatus()
                .isOk();

        await().atMost(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    var processed = meterRegistry
                            .counter(
                                    "sombra.exchange.processed",
                                    "outcome", "mismatch",
                                    "method", "POST",
                                    "status_class", "2xx")
                            .count();
                    assertThat(processed).isGreaterThanOrEqualTo(1);

                    var discrepancy = meterRegistry
                            .counter("sombra.discrepancy.count", "type", "ValueMismatch", "field_kind", "body")
                            .count();
                    assertThat(discrepancy).isGreaterThanOrEqualTo(1);
                });

        assertThat(appender.list).anyMatch(e -> {
            var kvs = keyValues(e);
            return Boolean.FALSE.equals(kvs.get("matched"))
                    && "/echo/different".equals(kvs.get("request_path"))
                    && "{\"name\":\"original\",\"value\":1}".equals(kvs.get("original_response_body"))
                    && "{\"name\":\"changed\",\"value\":42}".equals(kvs.get("candidate_response_body"))
                    && String.valueOf(kvs.get("discrepancy_summary")).contains("ValueMismatch");
        });
    }

    private static Map<String, Object> keyValues(ILoggingEvent event) {
        var pairs = event.getKeyValuePairs();
        if (pairs == null) {
            return Map.of();
        }
        Map<String, Object> map = new LinkedHashMap<>();
        pairs.forEach(p -> map.put(p.key, p.value));
        return map;
    }
}
