package com.github.vitorpereiraa.sombra.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.github.vitorpereiraa.sombra.SombraServerApplication;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;

// Standalone (not extending BaseIT) so its candidate-url is the only registration: it points at an
// unbound port, so every replay fails to connect. This exercises the orchestrator's error branch:
// reportError -> sombra.replay.errors counter + logReplayError.
@Import(TestcontainersConfiguration.class)
@SpringBootTest(classes = SombraServerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ReplayErrorIT {

    @LocalServerPort
    private int port;

    @Autowired
    private MeterRegistry meterRegistry;

    private RestTestClient client;

    @BeforeEach
    void setup() {
        client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @DynamicPropertySource
    static void configureSombra(DynamicPropertyRegistry registry) {
        registry.add("server.port", () -> String.valueOf(BaseIT.findFreePort()));
        registry.add("management.server.port", () -> String.valueOf(BaseIT.findFreePort()));
        registry.add("sombra.agent.enabled", () -> true);
        registry.add("sombra.agent.topic-name", () -> "sombra.captured-exchanges");
        registry.add("sombra.server.ingestion.topic-name", () -> "sombra.captured-exchanges");
        // Unbound port: every replay connection is refused.
        registry.add("sombra.server.replay.candidate-url", () -> "http://localhost:" + BaseIT.findFreePort());
        registry.add(
                "spring.kafka.producer.key-serializer", () -> "org.apache.kafka.common.serialization.StringSerializer");
        registry.add(
                "spring.kafka.producer.value-serializer",
                () -> "org.springframework.kafka.support.serializer.JacksonJsonSerializer");
    }

    @Test
    void shouldRecordReplayErrorWhenCandidateIsUnreachable() {
        client.post()
                .uri("/echo")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"name\":\"test\"}")
                .exchange()
                .expectStatus()
                .isOk();

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    var errors = meterRegistry.find("sombra.replay.errors").counters().stream()
                            .mapToDouble(Counter::count)
                            .sum();
                    assertThat(errors).isGreaterThanOrEqualTo(1);
                });
    }
}
