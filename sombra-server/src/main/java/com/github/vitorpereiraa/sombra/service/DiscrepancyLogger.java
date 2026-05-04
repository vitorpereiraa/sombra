package com.github.vitorpereiraa.sombra.service;

import com.github.vitorpereiraa.sombra.domain.capture.CapturedExchange;
import com.github.vitorpereiraa.sombra.domain.capture.TraceId;
import com.github.vitorpereiraa.sombra.domain.comparison.ComparisonResult;
import com.github.vitorpereiraa.sombra.domain.http.HttpBody;
import com.github.vitorpereiraa.sombra.domain.reporting.ComparisonReport;
import com.github.vitorpereiraa.sombra.domain.reporting.ReportedDiscrepancy;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DiscrepancyLogger {

    private static final Logger log = LoggerFactory.getLogger(DiscrepancyLogger.class);

    public void logComparison(CapturedExchange exchange, ComparisonResult result) {
        var candidate = result.candidateResponse();
        var report = new ComparisonReport.Compared(
                exchange.traceId().map(TraceId::value).orElse(null),
                exchange.request().method().name(),
                exchange.request().path().value(),
                exchange.response().statusCode().value(),
                exchange.response().duration().toNanos(),
                candidate.duration().toNanos(),
                exchange.response().body().map(HttpBody::content).orElse(null),
                candidate.statusCode().value(),
                result.matched(),
                candidate.body().map(HttpBody::content).orElse(null),
                result.discrepancies().stream().map(ReportedDiscrepancy::from).toList());

        var summary = summarize(report.discrepancies());
        var message = report.match()
                ? "match %s %s -> %d".formatted(report.method(), report.path(), report.candidateStatus())
                : "mismatch %s %s original=%d candidate=%d [%s]"
                        .formatted(
                                report.method(),
                                report.path(),
                                report.originalStatus(),
                                report.candidateStatus(),
                                summary);

        var event = report.match() ? log.atInfo() : log.atWarn();
        event.addKeyValue("event", "shadow.comparison")
                .addKeyValue("trace_id", report.traceId())
                .addKeyValue("request_method", report.method())
                .addKeyValue("request_path", report.path())
                .addKeyValue("matched", report.match())
                .addKeyValue("original_response_status", report.originalStatus())
                .addKeyValue("candidate_response_status", report.candidateStatus())
                .addKeyValue("original_response_duration_ns", report.originalDurationNs())
                .addKeyValue("candidate_response_duration_ns", report.replayDurationNs())
                .addKeyValue("discrepancy_count", report.discrepancies().size())
                .addKeyValue("discrepancy_summary", summary)
                .addKeyValue("discrepancies", toLogShape(report.discrepancies()))
                .addKeyValue("original_response_body", report.originalBody())
                .addKeyValue("candidate_response_body", report.candidateBody())
                .log(message);
    }

    private static List<Map<String, Object>> toLogShape(List<ReportedDiscrepancy> discrepancies) {
        return discrepancies.stream().map(d -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("type", d.type());
            m.put("field_kind", d.fieldKind());
            d.name().ifPresent(n -> m.put("name", n));
            d.path().ifPresent(p -> m.put("path", p));
            d.originalValue().ifPresent(v -> m.put("original_value", v));
            d.candidateValue().ifPresent(v -> m.put("candidate_value", v));
            return m;
        }).toList();
    }

    public void logReplayError(CapturedExchange exchange, Throwable error) {
        var report = new ComparisonReport.ReplayFailed(
                exchange.traceId().map(TraceId::value).orElse(null),
                exchange.request().method().name(),
                exchange.request().path().value(),
                exchange.response().statusCode().value(),
                exchange.response().duration().toNanos(),
                exchange.response().body().map(HttpBody::content).orElse(null),
                error.getClass().getSimpleName() + ": " + error.getMessage());

        log.atError()
                .addKeyValue("event", "shadow.replayFailed")
                .addKeyValue("trace_id", report.traceId())
                .addKeyValue("request_method", report.method())
                .addKeyValue("request_path", report.path())
                .addKeyValue("original_response_status", report.originalStatus())
                .addKeyValue("original_response_duration_ns", report.originalDurationNs())
                .addKeyValue("original_response_body", report.originalBody())
                .addKeyValue("error_type", error.getClass().getSimpleName())
                .addKeyValue("error", report.error())
                .log("replay failed {} {}: {}", report.method(), report.path(), report.error());
    }

    private static String summarize(List<ReportedDiscrepancy> discrepancies) {
        return discrepancies.stream().map(DiscrepancyLogger::summarize).collect(Collectors.joining(","));
    }

    private static String summarize(ReportedDiscrepancy d) {
        var qualifier = d.name().or(d::path).map(s -> ":" + s).orElse("");
        return d.type() + ":" + d.fieldKind() + qualifier;
    }
}
