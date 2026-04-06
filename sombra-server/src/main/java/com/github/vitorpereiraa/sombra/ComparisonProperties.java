package com.github.vitorpereiraa.sombra;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sombra.server.comparison")
public record ComparisonProperties(List<String> ignoredFields) {

    public ComparisonProperties {
        checkArgument(ignoredFields != null, "sombra.server.comparison.ignored-fields must be configured");
    }
}
