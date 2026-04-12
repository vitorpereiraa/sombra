package com.github.vitorpereiraa.sombra.reporting;

import static com.google.common.base.Preconditions.checkArgument;

import java.time.Duration;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sombra.reporting")
public record ReportingProperties(Metrics metrics, Logging logging) {

    public ReportingProperties {
        checkArgument(metrics != null, "sombra.reporting.metrics must be configured");
        checkArgument(logging != null, "sombra.reporting.logging must be configured");
    }

    public record Metrics(
            boolean enabled,
            String otlpEndpoint,
            Duration step,
            Map<String, String> resourceAttributes) {

        public Metrics {
            if (enabled) {
                checkArgument(
                        otlpEndpoint != null && !otlpEndpoint.isBlank(),
                        "sombra.reporting.metrics.otlp-endpoint must be set when metrics enabled");
                checkArgument(step != null, "sombra.reporting.metrics.step must be set when metrics enabled");
                checkArgument(!step.isNegative() && !step.isZero(), "sombra.reporting.metrics.step must be positive");
            }
            resourceAttributes = resourceAttributes == null ? Map.of() : Map.copyOf(resourceAttributes);
        }
    }

    public record Logging(boolean enabled, int maxValueLength, boolean includeMatchDetails) {

        public Logging {
            checkArgument(
                    maxValueLength > 0, "sombra.reporting.logging.max-value-length must be positive, got: %s", maxValueLength);
        }
    }
}
