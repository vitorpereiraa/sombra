package com.github.vitorpereiraa.sombra.reporting;

import io.micrometer.registry.otlp.OtlpConfig;
import java.time.Duration;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReportingConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "sombra.reporting.metrics", name = "enabled", havingValue = "true")
    public OtlpConfig otlpConfig(ReportingProperties properties) {
        var metrics = properties.metrics();
        return new OtlpConfig() {
            @Override
            public String url() {
                return metrics.otlpEndpoint();
            }

            @Override
            public Duration step() {
                return metrics.step();
            }

            @Override
            public Map<String, String> resourceAttributes() {
                return metrics.resourceAttributes();
            }

            @Override
            public String get(String key) {
                return null;
            }
        };
    }
}
