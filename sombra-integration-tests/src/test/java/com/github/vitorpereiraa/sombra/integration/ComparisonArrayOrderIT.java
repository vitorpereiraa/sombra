package com.github.vitorpereiraa.sombra.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.github.vitorpereiraa.sombra.service.ResponseComparisonService;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

class ComparisonArrayOrderIT extends BaseIT {

    @Autowired
    private ResponseComparisonService comparisonService;

    @DynamicPropertySource
    static void configureArrayOrder(DynamicPropertyRegistry registry) {
        registry.add("sombra.server.comparison.ignore-array-order", () -> true);
    }

    @Test
    void shouldMatchWhenArrayIsReorderedAndOrderIsIgnored() {
        client.post()
                .uri("/echo/array-reordered")
                .contentType(MediaType.APPLICATION_JSON)
                .body("[{\"id\":1},{\"id\":2}]")
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
