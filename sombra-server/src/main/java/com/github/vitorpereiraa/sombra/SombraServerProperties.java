package com.github.vitorpereiraa.sombra;

import static com.google.common.base.Preconditions.checkArgument;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sombra.server")
public record SombraServerProperties(String topicName, String candidateUrl) {

    public SombraServerProperties {
        checkArgument(topicName != null, "sombra.server.topic-name must be configured");
        checkArgument(!topicName.isBlank(), "sombra.server.topic-name cannot be blank");
        checkArgument(candidateUrl != null, "sombra.server.candidate-url must be configured");
        checkArgument(!candidateUrl.isBlank(), "sombra.server.candidate-url cannot be blank");
    }
}
