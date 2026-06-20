package com.github.vitorpereiraa.sombra.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

// Configures a nested ignored field (/user/name) and verifies that an exchange whose only
// difference is at that path is treated as a match, end-to-end through the comparison config.
// The ignored-fields list is supplied via @TestPropertySource (an enumerable source) because the
// binder cannot discover indexed list elements from @DynamicPropertySource's non-enumerable source.
@TestPropertySource(properties = "sombra.server.comparison.ignored-fields=/user/name")
class IgnoredFieldIT extends BaseIT {

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    void shouldMatchWhenTheOnlyDifferingFieldIsIgnored() {
        client.post()
                .uri("/echo/nested")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"user\":{\"id\":1,\"name\":\"original\"}}")
                .exchange()
                .expectStatus()
                .isOk();

        await().atMost(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    var match = meterRegistry
                            .counter(
                                    "sombra.exchange.processed",
                                    "outcome", "match",
                                    "method", "POST",
                                    "status_class", "2xx")
                            .count();
                    assertThat(match).isGreaterThanOrEqualTo(1);
                });
    }
}
