package com.github.vitorpereiraa.sombra.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

class ReportingIT extends BaseIT {

    @Autowired
    private MeterRegistry meterRegistry;

    private ListAppender<ILoggingEvent> appender;
    private ch.qos.logback.classic.Logger reportingLogger;

    @DynamicPropertySource
    static void enableMetrics(DynamicPropertyRegistry registry) {
        registry.add("sombra.reporting.metrics.enabled", () -> "true");
        registry.add("sombra.reporting.metrics.otlp-endpoint", () -> "http://localhost:4318/v1/metrics");
        registry.add("sombra.reporting.metrics.step", () -> "1h");
    }

    @BeforeEach
    void attachAppender() {
        var context = (LoggerContext) LoggerFactory.getILoggerFactory();
        reportingLogger = context.getLogger("com.github.vitorpereiraa.sombra.reporting.ExchangeLogger");
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

        await().atMost(Duration.ofSeconds(10))
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

        assertThat(appender.list)
                .anyMatch(e -> e.getFormattedMessage().contains("\"match\":true")
                        && e.getFormattedMessage().contains("\"path\":\"/echo\""));
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

        await().atMost(Duration.ofSeconds(10))
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

        assertThat(appender.list)
                .anyMatch(e -> {
                    var msg = e.getFormattedMessage();
                    return msg.contains("\"match\":false")
                            && msg.contains("\"path\":\"/echo/different\"")
                            && msg.contains("\"originalBody\":\"{\\\"name\\\":\\\"original\\\",\\\"value\\\":1}\"")
                            && msg.contains("\"candidateBody\":\"{\\\"name\\\":\\\"changed\\\",\\\"value\\\":42}\"")
                            && msg.contains("ValueMismatch");
                });
    }
}
