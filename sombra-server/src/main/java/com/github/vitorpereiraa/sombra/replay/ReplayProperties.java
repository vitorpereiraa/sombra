package com.github.vitorpereiraa.sombra.replay;

import static com.google.common.base.Preconditions.checkArgument;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sombra.server.replay")
public record ReplayProperties(String candidateUrl) {

    public ReplayProperties {
        checkArgument(candidateUrl != null, "sombra.server.replay.candidate-url must be configured");
        checkArgument(!candidateUrl.isBlank(), "sombra.server.replay.candidate-url cannot be blank");
    }
}
