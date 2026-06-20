package com.github.vitorpereiraa.sombra.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

// Exercises a non-2xx exchange end-to-end so the status_class metric tag is covered beyond the 2xx
// path the other reporting tests use.
class StatusClassIT extends BaseIT {

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    void shouldTagProcessedExchangeWithServerErrorStatusClass() {
        client.post()
                .uri("/echo/server-error")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"name\":\"test\"}")
                .exchange()
                .expectStatus()
                .is5xxServerError();

        await().atMost(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    var count = meterRegistry
                            .counter(
                                    "sombra.exchange.processed",
                                    "outcome", "match",
                                    "method", "POST",
                                    "status_class", "5xx")
                            .count();
                    assertThat(count).isGreaterThanOrEqualTo(1);
                });
    }
}
