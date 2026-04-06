package com.github.vitorpereiraa.sombra.integration;

import com.github.vitorpereiraa.sombra.agent.streaming.dto.CapturedExchangeEvent;
import com.github.vitorpereiraa.sombra.streaming.CapturedExchangeConsumer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

class CaptureFilterIT extends BaseIT {

    @MockitoSpyBean
    CapturedExchangeConsumer consumer;

    @Test
    void shouldCaptureRequestAndConsumeFromKafka() {
        client.post().uri("/echo")
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"hello\":\"world\"}")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .value(body -> assertThat(body).isEqualTo("{\"hello\":\"world\"}"));

        var captor = ArgumentCaptor.forClass(CapturedExchangeEvent.class);

        await().atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> verify(consumer).consume(captor.capture()));

        var event = captor.getValue();
        assertThat(event.request().method()).isEqualTo("POST");
        assertThat(event.request().path()).isEqualTo("/echo");
        assertThat(event.request().body()).contains("{\"hello\":\"world\"}");
        assertThat(event.response().statusCode()).isEqualTo(200);
        assertThat(event.response().body()).contains("{\"hello\":\"world\"}");
        assertThat(event.timestamp()).isNotNull();
    }
}
