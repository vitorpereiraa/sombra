package com.github.vitorpereiraa.sombra.integration;

import com.github.vitorpereiraa.sombra.SombraApplication;
import com.github.vitorpereiraa.sombra.agent.streaming.dto.CapturedExchangeEvent;
import com.github.vitorpereiraa.sombra.agent.streaming.dto.HttpRequestEvent;
import com.github.vitorpereiraa.sombra.agent.streaming.dto.HttpResponseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SpringBootTest(
    classes = SombraApplication.class,
    properties = {
        "sombra.server.topic-name=sombra.captured-exchanges",
        "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JacksonJsonSerializer"
    }
)
@Import(TestcontainersConfiguration.class)
public abstract class BaseIT {

    @Autowired
    protected KafkaTemplate<String, CapturedExchangeEvent> kafkaTemplate;

    protected static CapturedExchangeEvent exchangeEvent(
            String method,
            String path,
            int statusCode,
            Map<String, List<String>> requestHeaders,
            Map<String, List<String>> responseHeaders,
            Optional<String> requestBody,
            Optional<String> responseBody,
            Optional<String> traceId) {
        var request = new HttpRequestEvent(method, path, requestHeaders, requestBody);
        var response = new HttpResponseEvent(statusCode, responseHeaders, responseBody);
        return new CapturedExchangeEvent(request, response, Instant.now(), traceId);
    }

    protected static CapturedExchangeEvent defaultExchangeEvent() {
        return exchangeEvent(
            "GET", "/api/users/1", 200,
            Map.of("Accept", List.of("application/json")),
            Map.of("Content-Type", List.of("application/json")),
            Optional.empty(),
            Optional.of("{\"id\":1}"),
            Optional.of("trace-123")
        );
    }
}
