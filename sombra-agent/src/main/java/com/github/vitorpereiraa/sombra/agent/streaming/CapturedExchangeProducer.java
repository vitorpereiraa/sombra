package com.github.vitorpereiraa.sombra.agent.streaming;

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

    public void send(CapturedExchangeEvent event) {
        kafkaTemplate.send(topicName, event.request().path(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to send captured exchange for {} {}",
                        event.request().method(), event.request().path(), ex);
                } else {
                    log.debug("Sent captured exchange for {} {} to topic {}",
                        event.request().method(), event.request().path(), topicName);
                }
            });
    }
}
