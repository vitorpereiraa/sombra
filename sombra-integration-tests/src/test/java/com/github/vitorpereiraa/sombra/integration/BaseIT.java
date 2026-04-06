package com.github.vitorpereiraa.sombra.integration;

import com.github.vitorpereiraa.sombra.SombraApplication;
import com.github.vitorpereiraa.sombra.agent.streaming.dto.CapturedExchangeEvent;
import com.github.vitorpereiraa.sombra.agent.streaming.dto.HttpRequestEvent;
import com.github.vitorpereiraa.sombra.agent.streaming.dto.HttpResponseEvent;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(classes = SombraApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIT {

    @LocalServerPort
    private int port;

    protected RestTestClient client;

    @BeforeEach
    public void setup() {
        client = RestTestClient.bindToServer()
            .baseUrl("http://localhost:" + port)
            .build();
    }

    @DynamicPropertySource
    static void configureSombra(DynamicPropertyRegistry registry) {
        registry.add("sombra.agent.enabled", () -> true);
        registry.add("sombra.agent.topic-name", () -> "sombra.captured-exchanges");
        registry.add("sombra.server.topic-name", () -> "sombra.captured-exchanges");
        registry.add("spring.kafka.producer.key-serializer", () -> "org.apache.kafka.common.serialization.StringSerializer");
        registry.add("spring.kafka.producer.value-serializer", () -> "org.springframework.kafka.support.serializer.JacksonJsonSerializer");
    }
}
