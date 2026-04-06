package com.github.vitorpereiraa.sombra.streaming;

import com.github.vitorpereiraa.sombra.agent.streaming.dto.CapturedExchangeEvent;
import com.github.vitorpereiraa.sombra.domain.CapturedExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CapturedExchangeConsumer {

    private static final Logger log = LoggerFactory.getLogger(CapturedExchangeConsumer.class);

    @KafkaListener(topics = "${sombra.server.topic-name}")
    public void consume(CapturedExchangeEvent event) {
        var exchange = CapturedExchangeMapper.toDomain(event);
        log.info("Received captured exchange: {} {} -> {}",
            exchange.request().method(),
            exchange.request().path().value(),
            exchange.response().statusCode().value());
    }
}
