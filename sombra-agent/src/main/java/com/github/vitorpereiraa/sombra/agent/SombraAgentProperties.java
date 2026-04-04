package com.github.vitorpereiraa.sombra.agent;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sombra.agent")
public record SombraAgentProperties(
    boolean enabled,
    String topicName
) {

    public SombraAgentProperties {
        if (topicName == null || topicName.isBlank()) {
            topicName = "sombra.captured-exchanges";
        }
    }
}
