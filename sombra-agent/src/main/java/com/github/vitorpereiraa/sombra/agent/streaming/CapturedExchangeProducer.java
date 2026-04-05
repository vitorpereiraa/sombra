package com.github.vitorpereiraa.sombra.agent.streaming;

import com.github.vitorpereiraa.sombra.agent.domain.CapturedExchange;
import com.github.vitorpereiraa.sombra.agent.streaming.dto.CapturedExchangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

public class CapturedExchangeProducer {

    private static final Logger log = LoggerFactory.getLogger(CapturedExchangeProducer.class);

    private final KafkaTemplate<String, CapturedExchangeEvent> kafkaTemplate;
    private final String topicName;

    public CapturedExchangeProducer(KafkaTemplate<String, CapturedExchangeEvent> kafkaTemplate, String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    public void send(CapturedExchange exchange) {
        var event = CapturedExchangeEvent.from(exchange);
        kafkaTemplate.send(topicName, exchange.request().path().value(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to send captured exchange for {} {}",
                        exchange.request().method(), exchange.request().path().value(), ex);
                } else {
                    log.debug("Sent captured exchange for {} {} to topic {}",
                        exchange.request().method(), exchange.request().path().value(), topicName);
                }
            });
    }
}
