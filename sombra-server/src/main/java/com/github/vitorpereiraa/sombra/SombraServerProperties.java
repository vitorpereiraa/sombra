package com.github.vitorpereiraa.sombra;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static com.google.common.base.Preconditions.checkArgument;

@ConfigurationProperties(prefix = "sombra.server")
public record SombraServerProperties(
    String topicName
) {

    public SombraServerProperties {
        checkArgument(topicName != null, "sombra.server.topic-name must be configured");
        checkArgument(!topicName.isBlank(), "sombra.server.topic-name cannot be blank");
    }
}
