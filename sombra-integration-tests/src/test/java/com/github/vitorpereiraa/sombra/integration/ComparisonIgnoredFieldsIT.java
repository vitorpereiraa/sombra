package com.github.vitorpereiraa.sombra.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.github.vitorpereiraa.sombra.service.ResponseComparisonService;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

class ComparisonIgnoredFieldsIT extends BaseIT {

    @Autowired
    private ResponseComparisonService comparisonService;

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
                    var result = comparisonService.lastResult();
                    assertThat(result).isNotNull();
                    assertThat(result.matched()).isTrue();
                });
    }
}
