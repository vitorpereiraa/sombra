package com.github.vitorpereiraa.sombra.agent;

import com.github.vitorpereiraa.sombra.agent.streaming.dto.CapturedExchangeEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.kafka.KafkaContainer;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class CaptureFilterIntegrationTest {

    private static final String TOPIC = "sombra.captured-exchanges";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    KafkaContainer kafkaContainer;

    @Test
    void shouldCaptureExchangeAndPublishToKafka() throws Exception {
        mockMvc.perform(post("/echo")
                .contentType("application/json")
                .content("{\"hello\":\"world\"}"))
            .andExpect(status().isOk());

        try (var consumer = createConsumer()) {
            consumer.subscribe(List.of(TOPIC));
            var records = consumer.poll(Duration.ofSeconds(10));

            assertThat(records).isNotEmpty();
            var event = records.iterator().next().value();
            assertThat(event.request().method()).isEqualTo("POST");
            assertThat(event.request().path()).isEqualTo("/echo");
            assertThat(event.request().body()).contains("{\"hello\":\"world\"}");
            assertThat(event.response().statusCode()).isEqualTo(200);
            assertThat(event.response().body()).contains("{\"hello\":\"world\"}");
            assertThat(event.timestamp()).isNotNull();
            assertThat(event.traceId()).isEmpty();
        }
    }

    KafkaConsumer<String, CapturedExchangeEvent> createConsumer() {
        var deserializer = new JacksonJsonDeserializer<>(CapturedExchangeEvent.class);
        deserializer.addTrustedPackages("com.github.vitorpereiraa.sombra.agent.streaming.dto");

        var props = Map.<String, Object>of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers(),
            ConsumerConfig.GROUP_ID_CONFIG, "test-group",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class
        );
        return new KafkaConsumer<>(props, new StringDeserializer(), deserializer);
    }
}
