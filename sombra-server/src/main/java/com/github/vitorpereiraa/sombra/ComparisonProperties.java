package com.github.vitorpereiraa.sombra;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "sombra.server.comparison")
public record ComparisonProperties(
        @DefaultValue Optional<List<String>> ignoredFields,
        @DefaultValue Optional<Boolean> ignoreArrayOrder,
        @DefaultValue Optional<Boolean> compareHeaders,
        @DefaultValue Optional<List<String>> ignoredHeaders) {

    public ComparisonProperties {
        checkNotNull(ignoredFields, "ignoredFields Optional cannot be null");
        checkNotNull(ignoreArrayOrder, "ignoreArrayOrder Optional cannot be null");
        checkNotNull(compareHeaders, "compareHeaders Optional cannot be null");
        checkNotNull(ignoredHeaders, "ignoredHeaders Optional cannot be null");
        ignoredFields = ignoredFields.map(List::copyOf);
        ignoredHeaders = ignoredHeaders.map(List::copyOf);
    }
}
