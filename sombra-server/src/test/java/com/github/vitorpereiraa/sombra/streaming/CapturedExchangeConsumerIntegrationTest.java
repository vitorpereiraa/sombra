package com.github.vitorpereiraa.sombra.streaming;

import com.github.vitorpereiraa.sombra.TestcontainersConfiguration;
import com.github.vitorpereiraa.sombra.agent.streaming.dto.CapturedExchangeEvent;
import com.github.vitorpereiraa.sombra.agent.streaming.dto.HttpRequestEvent;
import com.github.vitorpereiraa.sombra.agent.streaming.dto.HttpResponseEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class CapturedExchangeConsumerIntegrationTest {

    @Autowired
    KafkaTemplate<String, CapturedExchangeEvent> kafkaTemplate;

    @MockitoSpyBean
    CapturedExchangeConsumer consumer;

    @Test
    void shouldConsumeExchangeFromKafka() {
        var request = new HttpRequestEvent("GET", "/api/users/1",
            Map.of("Accept", List.of("application/json")), Optional.empty());
        var response = new HttpResponseEvent(200,
            Map.of("Content-Type", List.of("application/json")), Optional.of("{\"id\":1}"));
        var event = new CapturedExchangeEvent(request, response, Instant.now(), Optional.of("trace-123"));

        kafkaTemplate.send("sombra.captured-exchanges", event);

        await().atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> verify(consumer).consume(any(CapturedExchangeEvent.class)));
    }
}
