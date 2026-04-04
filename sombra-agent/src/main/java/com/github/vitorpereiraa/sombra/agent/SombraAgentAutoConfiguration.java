package com.github.vitorpereiraa.sombra.agent;

import com.github.vitorpereiraa.sombra.agent.service.CaptureFilter;
import com.github.vitorpereiraa.sombra.agent.streaming.CapturedExchangeProducer;
import com.github.vitorpereiraa.sombra.agent.streaming.dto.CapturedExchangeEvent;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.kafka.core.KafkaTemplate;

@AutoConfiguration
@ConditionalOnProperty(name = "sombra.agent.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SombraAgentProperties.class)
public class SombraAgentAutoConfiguration {

    @Bean
    CapturedExchangeProducer capturedExchangeProducer(
            KafkaTemplate<String, CapturedExchangeEvent> kafkaTemplate,
            SombraAgentProperties properties) {
        return new CapturedExchangeProducer(kafkaTemplate, properties.topicName());
    }

    @Bean
    FilterRegistrationBean<CaptureFilter> sombraCaptureFilter(CapturedExchangeProducer producer) {
        var registration = new FilterRegistrationBean<>(new CaptureFilter(producer));
        registration.setOrder(Ordered.LOWEST_PRECEDENCE - 1);
        registration.addUrlPatterns("/*");
        return registration;
    }
}
