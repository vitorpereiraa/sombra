package com.github.vitorpereiraa.sombra.config;

import static com.google.common.base.Preconditions.checkArgument;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sombra.reporting")
public record ReportingProperties(Logging logging) {

    public ReportingProperties {
        checkArgument(logging != null, "sombra.reporting.logging must be configured");
    }

    public record Logging(boolean enabled, int maxValueLength) {

        public Logging {
            checkArgument(
                    maxValueLength > 0, "sombra.reporting.logging.max-value-length must be positive, got: %s", maxValueLength);
        }
    }
}
