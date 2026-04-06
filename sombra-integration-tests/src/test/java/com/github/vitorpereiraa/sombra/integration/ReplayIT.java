package com.github.vitorpereiraa.sombra.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import com.github.vitorpereiraa.sombra.agent.streaming.dto.CapturedExchangeEvent;
import com.github.vitorpereiraa.sombra.agent.streaming.dto.HttpRequestEvent;
import com.github.vitorpereiraa.sombra.agent.streaming.dto.HttpResponseEvent;
import com.github.vitorpereiraa.sombra.domain.HttpRequest;
import com.github.vitorpereiraa.sombra.domain.HttpResponse;
import com.github.vitorpereiraa.sombra.service.CandidateReplayService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

class ReplayIT extends BaseIT {

    @MockitoSpyBean
    CandidateReplayService replayService;

    @Autowired
    KafkaTemplate<String, CapturedExchangeEvent> kafkaTemplate;

    @Test
    void shouldReplayRequestToCandidate() {
        var responseHolder = new AtomicReference<HttpResponse>();
        doAnswer(invocation -> {
                    var result = (HttpResponse) invocation.callRealMethod();
                    responseHolder.set(result);
                    return result;
                })
                .when(replayService)
                .replay(any(HttpRequest.class));

        var event = new CapturedExchangeEvent(
                new HttpRequestEvent(
                        "POST",
                        "/echo",
                        Map.of("Content-Type", List.of("application/json")),
                        Optional.of("{\"test\":\"replay\"}")),
                new HttpResponseEvent(200, Map.of(), Optional.of("{\"test\":\"replay\"}")),
                Instant.now(),
                Optional.empty());

        kafkaTemplate.send("sombra.captured-exchanges", event);

        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertThat(responseHolder.get()).isNotNull());

        assertThat(responseHolder.get().statusCode().value()).isEqualTo(200);
        assertThat(responseHolder.get().body()).isPresent();
        assertThat(responseHolder.get().body().get().content()).isEqualTo("{\"test\":\"replay\"}");
    }
}
