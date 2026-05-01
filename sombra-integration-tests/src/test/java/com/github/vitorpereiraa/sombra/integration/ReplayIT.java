package com.github.vitorpereiraa.sombra.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ReplayIT extends BaseIT {

    @Test
    void shouldReplayRequestToCandidate() {
        client.post().uri("/echo/replay")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"test\":\"replay\"}")
                .exchange()
                .expectStatus().isOk();

        await().atMost(Duration.ofMillis(500))
                .untilAsserted(() -> client.get().uri("/echo/replay/count")
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody(Integer.class)
                        .value(count -> assertThat(count).isEqualTo(2)));
    }
}
