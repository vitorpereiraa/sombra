package com.github.vitorpereiraa.sombra.reporting;

import java.util.List;

sealed interface ComparisonReport {

    String traceId();

    String method();

    String path();

    int originalStatus();

    long originalDurationMs();

    long replayDurationMs();

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
            long replayDurationMs,
            String originalBody,
            String error)
            implements ComparisonReport {}
}
