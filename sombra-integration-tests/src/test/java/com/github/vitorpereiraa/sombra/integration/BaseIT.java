package com.github.vitorpereiraa.sombra.integration;

import com.github.vitorpereiraa.sombra.SombraApplication;
import java.io.IOException;
import java.net.ServerSocket;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(classes = SombraApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
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
        int port = findFreePort();
        registry.add("server.port", () -> String.valueOf(port));
        registry.add("sombra.agent.enabled", () -> true);
        registry.add("sombra.agent.topic-name", () -> "sombra.captured-exchanges");
        registry.add("sombra.server.topic-name", () -> "sombra.captured-exchanges");
        registry.add("sombra.server.candidate-url", () -> "http://localhost:" + port);
        registry.add("sombra.reporting.logging.include-match-details", () -> "true");
        registry.add(
                "spring.kafka.producer.key-serializer", () -> "org.apache.kafka.common.serialization.StringSerializer");
        registry.add(
                "spring.kafka.producer.value-serializer",
                () -> "org.springframework.kafka.support.serializer.JacksonJsonSerializer");
    }

    static int findFreePort() {
        try (var socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException("Failed to find a free port", e);
        }
    }
}
