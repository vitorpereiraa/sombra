package com.github.vitorpereiraa.sombra.service;

import com.github.vitorpereiraa.sombra.config.ReportingProperties;
import com.github.vitorpereiraa.sombra.domain.capture.CapturedExchange;
import com.github.vitorpereiraa.sombra.domain.capture.TraceId;
import com.github.vitorpereiraa.sombra.domain.comparison.ComparisonResult;
import com.github.vitorpereiraa.sombra.domain.http.HttpBody;
import com.github.vitorpereiraa.sombra.domain.reporting.ComparisonReport;
import com.github.vitorpereiraa.sombra.domain.reporting.ReportedDiscrepancy;
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

    public void logComparison(CapturedExchange exchange, ComparisonResult result) {
        var candidate = result.candidateResponse();
        var report = new ComparisonReport.Compared(
                exchange.traceId().map(TraceId::value).orElse(null),
                exchange.request().method().name(),
                exchange.request().path().value(),
                exchange.response().statusCode().value(),
                exchange.response().duration().toMillis(),
                candidate.duration().toMillis(),
                truncate(exchange.response().body().map(HttpBody::content).orElse(null)),
                candidate.statusCode().value(),
                result.matched(),
                truncate(candidate.body().map(HttpBody::content).orElse(null)),
                result.discrepancies().stream().map(ReportedDiscrepancy::from).toList());

        try {
            log.info(jsonMapper.writeValueAsString(report));
        } catch (Exception e) {
            log.warn("Failed to serialize exchange log record", e);
        }
    }

    public void logReplayError(CapturedExchange exchange, Throwable error) {
        var report = new ComparisonReport.ReplayFailed(
                exchange.traceId().map(TraceId::value).orElse(null),
                exchange.request().method().name(),
                exchange.request().path().value(),
                exchange.response().statusCode().value(),
                exchange.response().duration().toMillis(),
                truncate(exchange.response().body().map(HttpBody::content).orElse(null)),
                error.getClass().getSimpleName() + ": " + error.getMessage()
        );

        try {
            log.info(jsonMapper.writeValueAsString(report));
        } catch (Exception e) {
            log.warn("Failed to serialize exchange log record", e);
        }
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        int max = config.maxValueLength();
        if (value.length() <= max) {
            return value;
        }
        int end = max;
        if (Character.isHighSurrogate(value.charAt(end - 1))) {
            end--;
        }
        return value.substring(0, end) + "...";
    }
}
