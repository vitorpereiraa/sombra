package com.github.vitorpereiraa.sombra.agent;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@ConfigurationProperties(prefix = "sombra.agent")
public record SombraAgentProperties(
    boolean enabled,
    String topicName
) {

    public SombraAgentProperties {
        checkNotNull(topicName, "sombra.agent.topic-name must be configured");
        checkArgument(!topicName.isBlank(), "sombra.agent.topic-name cannot be blank");
    }
}
