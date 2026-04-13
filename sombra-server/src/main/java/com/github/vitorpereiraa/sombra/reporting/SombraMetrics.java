package com.github.vitorpereiraa.sombra.reporting;

import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class SombraMetrics {

    private final MeterRegistry registry;

    public SombraMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordProcessed(String outcome, String method, String statusClass) {
        registry.counter(
                        "sombra.exchange.processed",
                        "outcome", outcome,
                        "method", method,
                        "status_class", statusClass)
                .increment();
    }

    public void recordDiscrepancy(String type, String fieldKind) {
        registry.counter("sombra.discrepancy.count", "type", type, "field_kind", fieldKind).increment();
    }

    public void recordReplayDuration(Duration duration, String method, String statusClass, String outcome) {
        registry.timer(
                        "sombra.replay.duration",
                        "method", method,
                        "status_class", statusClass,
                        "outcome", outcome)
                .record(duration);
    }

    public void recordReplayErrorDuration(Duration duration, String method) {
        registry.timer("sombra.replay.duration", "method", method, "outcome", "error").record(duration);
    }

    public void recordReplayError() {
        registry.counter("sombra.replay.errors").increment();
    }

    public void recordComparisonDuration(Duration duration) {
        registry.timer("sombra.comparison.duration").record(duration);
    }

    public void recordOriginalDuration(Duration duration, String method, String statusClass) {
        registry.timer("sombra.original.duration", "method", method, "status_class", statusClass)
                .record(duration);
    }
}
