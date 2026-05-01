package com.github.vitorpereiraa.sombra.domain.reporting;

import java.util.List;

public sealed interface ComparisonReport {

    String traceId();

    String method();

    String path();

    int originalStatus();

    long originalDurationNs();

    String originalBody();

    record Compared(
            String traceId,
            String method,
            String path,
            int originalStatus,
            long originalDurationNs,
            long replayDurationNs,
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
            long originalDurationNs,
            String originalBody,
            String error)
            implements ComparisonReport {}
}
