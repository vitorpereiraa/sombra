package com.github.vitorpereiraa.sombra.agent.streaming;

import com.github.vitorpereiraa.sombra.agent.domain.CapturedExchange;
import com.github.vitorpereiraa.sombra.agent.streaming.dto.CapturedExchangeEvent;
import com.github.vitorpereiraa.sombra.agent.streaming.dto.HttpRequestEvent;
import com.github.vitorpereiraa.sombra.agent.streaming.dto.HttpResponseEvent;
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
        var event = toEvent(exchange);
        kafkaTemplate.send(topicName, exchange.path(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to send captured exchange for {} {}: {}",
                        exchange.method(), exchange.path(), ex.getMessage());
                } else {
                    log.debug("Sent captured exchange for {} {} to topic {}",
                        exchange.method(), exchange.path(), topicName);
                }
            });
    }

    private CapturedExchangeEvent toEvent(CapturedExchange exchange) {
        return new CapturedExchangeEvent(
            new HttpRequestEvent(exchange.method(), exchange.path(), exchange.requestHeaders(), exchange.requestBody()),
            new HttpResponseEvent(exchange.statusCode(), exchange.responseHeaders(), exchange.responseBody()),
            exchange.timestamp(),
            exchange.traceId()
        );
    }
}
