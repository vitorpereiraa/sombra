package com.github.vitorpereiraa.sombra.reporting;

import com.github.vitorpereiraa.sombra.domain.capture.CapturedExchange;
import com.github.vitorpereiraa.sombra.domain.capture.TraceId;
import com.github.vitorpereiraa.sombra.domain.comparison.ComparisonResult;
import com.github.vitorpereiraa.sombra.domain.comparison.Discrepancy;
import com.github.vitorpereiraa.sombra.domain.comparison.ResponseField;
import com.github.vitorpereiraa.sombra.domain.http.HttpBody;
import com.github.vitorpereiraa.sombra.domain.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class ExchangeLogger {

    private static final Logger log = LoggerFactory.getLogger(ExchangeLogger.class);

    private final ReportingProperties.Logging config;
    private final JsonMapper jsonMapper;

    public ExchangeLogger(ReportingProperties properties, JsonMapper jsonMapper) {
        this.config = properties.logging();
        this.jsonMapper = jsonMapper;
    }

    public void log(
            CapturedExchange exchange,
            HttpResponse candidate,
            ComparisonResult result,
            Duration replayDuration,
            Throwable error) {

        var record = new LinkedHashMap<String, Object>();
        record.put("ts", Instant.now().toString());
        record.put("traceId", exchange.traceId().map(TraceId::value).orElse(null));
        record.put("method", exchange.request().method().name());
        record.put("path", exchange.request().path().value());
        record.put("originalStatus", exchange.response().statusCode().value());
        record.put("candidateStatus", candidate == null ? null : candidate.statusCode().value());
        record.put("match", result != null && result.matched());
        record.put("originalDurationMs", exchange.response().duration().toMillis());
        record.put("replayDurationMs", replayDuration == null ? null : replayDuration.toMillis());
        record.put(
                "originalBody", truncate(exchange.response().body().map(HttpBody::content).orElse(null)));
        record.put(
                "candidateBody",
                candidate == null ? null : truncate(candidate.body().map(HttpBody::content).orElse(null)));
        record.put("discrepancyCount", result == null ? 0 : result.discrepancies().size());
        record.put("discrepancies", result == null ? List.of() : result.discrepancies().stream().map(this::toMap).toList());
        record.put("error", error == null ? null : error.getClass().getSimpleName() + ": " + error.getMessage());

        String json;
        try {
            json = jsonMapper.writeValueAsString(record);
        } catch (Exception e) {
            log.warn("Failed to serialize exchange log record", e);
            return;
        }

        boolean mismatchOrError = error != null || (result != null && !result.matched());
        if (mismatchOrError || config.includeMatchDetails()) {
            log.info(json);
        } else {
            log.debug(json);
        }
    }

    private Map<String, Object> toMap(Discrepancy discrepancy) {
        var entry = new LinkedHashMap<String, Object>();
        entry.put("type", discrepancy.getClass().getSimpleName());
        var field = discrepancy.field();
        entry.put("fieldKind", ReportingTags.fieldKind(field));
        if (field instanceof ResponseField.Header header) {
            entry.put("name", header.name());
        } else if (field instanceof ResponseField.Body body) {
            body.path().ifPresent(path -> entry.put("path", path.value()));
        }
        return entry;
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        int max = config.maxValueLength();
        return value.length() > max ? value.substring(0, max) + "..." : value;
    }
}
