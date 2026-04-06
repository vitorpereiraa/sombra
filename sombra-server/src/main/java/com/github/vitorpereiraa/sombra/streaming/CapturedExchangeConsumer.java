package com.github.vitorpereiraa.sombra.streaming;

import com.github.vitorpereiraa.sombra.agent.streaming.dto.CapturedExchangeEvent;
import com.github.vitorpereiraa.sombra.service.CandidateReplayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CapturedExchangeConsumer {

    private static final Logger log = LoggerFactory.getLogger(CapturedExchangeConsumer.class);

    private final CandidateReplayService replayService;

    public CapturedExchangeConsumer(CandidateReplayService replayService) {
        this.replayService = replayService;
    }

    @KafkaListener(topics = "${sombra.server.topic-name}")
    public void consume(CapturedExchangeEvent event) {
        var exchange = CapturedExchangeMapper.toDomain(event);
        log.info(
                "Received captured exchange: {} {} -> {}",
                exchange.request().method(),
                exchange.request().path().value(),
                exchange.response().statusCode().value());
        try {
            var replayedResponse = replayService.replay(exchange.request());
            log.info(
                    "Replayed exchange: {} {} -> {}",
                    exchange.request().method(),
                    exchange.request().path().value(),
                    replayedResponse.statusCode().value());
        } catch (Exception e) {
            log.error(
                    "Failed to replay exchange: {} {}",
                    exchange.request().method(),
                    exchange.request().path().value(),
                    e);
        }
    }
}
