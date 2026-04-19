package com.github.vitorpereiraa.sombra.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.github.vitorpereiraa.sombra.domain.comparison.ResponseField;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ComparisonIT extends BaseIT {

    @Test
    void shouldMatchWhenResponsesAreEqual() {
        client.post()
                .uri("/echo")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"name\":\"test\",\"value\":1}")
                .exchange()
                .expectStatus()
                .isOk();

        await().atMost(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    var result = lastComparisonResult();
                    assertThat(result).isNotNull();
                    assertThat(result.matched()).isTrue();
                });
    }

    @Test
    void shouldDetectDiscrepanciesWhenResponsesDiffer() {
        client.post()
                .uri("/echo/different")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"name\":\"original\",\"value\":1}")
                .exchange()
                .expectStatus()
                .isOk();

        await().atMost(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    var result = lastComparisonResult();
                    assertThat(result).isNotNull();
                    assertThat(result.matched()).isFalse();
                    assertThat(result.discrepancies())
                            .anyMatch(d -> d.field() instanceof ResponseField.Body body
                                    && body.path().isPresent()
                                    && body.path().get().value().equals("/name"));
                });
    }
}
