package com.github.vitorpereiraa.sombra.integration.streaming;

import com.github.vitorpereiraa.sombra.agent.streaming.dto.CapturedExchangeEvent;
import com.github.vitorpereiraa.sombra.integration.BaseIT;
import com.github.vitorpereiraa.sombra.streaming.CapturedExchangeConsumer;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

class CapturedExchangeConsumerIT extends BaseIT {

    @MockitoSpyBean
    CapturedExchangeConsumer consumer;

    @Test
    void shouldConsumeExchangeFromKafka() {
        kafkaTemplate.send("sombra.captured-exchanges", defaultExchangeEvent());

        await().atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> verify(consumer).consume(any(CapturedExchangeEvent.class)));
    }
}
