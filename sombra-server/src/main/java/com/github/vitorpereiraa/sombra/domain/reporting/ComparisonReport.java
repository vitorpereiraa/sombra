package com.github.vitorpereiraa.sombra.domain.reporting;

import java.util.List;

public sealed interface ComparisonReport {

    String traceId();

    String method();

    String path();

    int originalStatus();

    long originalDurationMs();

    String originalBody();

    record Compared(
            String traceId,
            String method,
            String path,
            int originalStatus,
            long originalDurationMs,
            long replayDurationMs,
            String originalBody,
            int candidateStatus,
            boolean match,
            String candidateBody,
            List<ReportedDiscrepancy> discrepancies)
            implements ComparisonReport {}

    record ReplayFailed(
            String traceId,
            String method,
            String path,
            int originalStatus,
            long originalDurationMs,
            String originalBody,
            String error)
            implements ComparisonReport {}
}
