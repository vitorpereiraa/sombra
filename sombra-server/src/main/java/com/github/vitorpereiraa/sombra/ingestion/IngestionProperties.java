package com.github.vitorpereiraa.sombra.ingestion;

import static com.google.common.base.Preconditions.checkArgument;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sombra.server.ingestion")
public record IngestionProperties(String topicName) {

    public IngestionProperties {
        checkArgument(topicName != null, "sombra.server.ingestion.topic-name must be configured");
        checkArgument(!topicName.isBlank(), "sombra.server.ingestion.topic-name cannot be blank");
    }
}
