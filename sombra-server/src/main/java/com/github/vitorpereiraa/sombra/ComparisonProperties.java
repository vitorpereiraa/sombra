package com.github.vitorpereiraa.sombra;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sombra.server.comparison")
public record ComparisonProperties(
        List<String> ignoredFields, boolean ignoreArrayOrder, boolean compareHeaders, List<String> ignoredHeaders) {

    public ComparisonProperties {
        if (ignoredFields == null) {
            ignoredFields = List.of();
        }
        if (ignoredHeaders == null) {
            ignoredHeaders = List.of();
        }
    }
}
