package com.github.vitorpereiraa.sombra.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

class ComparisonIgnoredFieldsIT extends BaseIT {

    @DynamicPropertySource
    static void configureIgnoredFields(DynamicPropertyRegistry registry) {
        registry.add("sombra.server.comparison.ignored-fields", () -> List.of("/name", "/value"));
    }

    @Test
    void shouldIgnoreConfiguredFieldsInComparison() {
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
                    assertThat(result.matched()).isTrue();
                });
    }
}
