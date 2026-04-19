package com.github.vitorpereiraa.sombra.service;

import com.github.vitorpereiraa.sombra.domain.http.HttpMethod;
import com.github.vitorpereiraa.sombra.domain.http.StatusCode;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class SombraMetrics {

    private final MeterRegistry registry;

    public SombraMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordOriginalDuration(HttpMethod method, Duration originalDuration, StatusCode originalStatusCode) {
        registry.timer(
                "sombra.original.duration",
                        "method", method.name(),
                        "status_class", originalStatusCode.statusClass()
                )
                .record(originalDuration);
    }

    public void recordReplayDuration(
            HttpMethod method, Duration candidateDuration, StatusCode candidateStatusCode, String outcome) {
        registry.timer(
                        "sombra.replay.duration",
                        "method", method.name(),
                        "status_class", candidateStatusCode.statusClass(),
                        "outcome", outcome)
                .record(candidateDuration);
    }


    public void recordProcessed(String outcome, HttpMethod method, StatusCode candidateStatusCode) {
        registry.counter(
                        "sombra.exchange.processed",
                        "outcome", outcome,
                        "method", method.name(),
                        "status_class", candidateStatusCode.statusClass())
                .increment();
    }

    public void recordDiscrepancy(String type, String fieldKind) {
        registry.counter("sombra.discrepancy.count", "type", type, "field_kind", fieldKind).increment();
    }

    public void recordReplayError() {
        registry.counter("sombra.replay.errors").increment();
    }

}
