package com.github.vitorpereiraa.sombra;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sombra.server.comparison")
public record ComparisonProperties(List<String> ignoredFields) {

    public ComparisonProperties {
        if (ignoredFields == null) {
            ignoredFields = List.of();
        }
    }
}
