package com.github.vitorpereiraa.sombra.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.github.vitorpereiraa.sombra.domain.comparison.Discrepancy;
import com.github.vitorpereiraa.sombra.domain.comparison.ResponseField;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ComparisonNonJsonIT extends BaseIT {

    @Test
    void shouldReportTypeMismatchWhenOneSideIsNotJson() {
        client.post()
                .uri("/echo/non-json")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"name\":\"original\"}")
                .exchange()
                .expectStatus()
                .isOk();

        await().atMost(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    var result = lastComparisonResult();
                    assertThat(result).isNotNull();
                    assertThat(result.matched()).isFalse();
                    assertThat(result.discrepancies())
                            .anyMatch(d -> d instanceof Discrepancy.TypeMismatch tm
                                    && tm.field() instanceof ResponseField.Body body
                                    && body.path().isEmpty());
                });
    }
}
