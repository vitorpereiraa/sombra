package com.github.vitorpereiraa.sombra.ingestion;

import com.github.vitorpereiraa.sombra.agent.streaming.dto.CapturedExchangeEvent;
import com.github.vitorpereiraa.sombra.orchestrator.Orchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CapturedExchangeConsumer {

    private static final Logger log = LoggerFactory.getLogger(CapturedExchangeConsumer.class);

    private final Orchestrator orchestrator;

    public CapturedExchangeConsumer(Orchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @KafkaListener(topics = "${sombra.server.ingestion.topic-name}")
    public void consume(CapturedExchangeEvent event) {
        log.debug("Received exchange: {} {} -> {}",
                event.request().method(),
                event.request().path(),
                event.response().statusCode()
        );

        var exchange = CapturedExchangeMapper.toDomain(event);

        orchestrator.process(exchange);
    }
}
