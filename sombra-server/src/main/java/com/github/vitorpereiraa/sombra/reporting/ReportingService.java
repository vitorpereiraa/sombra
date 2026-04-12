package com.github.vitorpereiraa.sombra.reporting;

import com.github.vitorpereiraa.sombra.domain.capture.CapturedExchange;
import com.github.vitorpereiraa.sombra.domain.comparison.ComparisonResult;
import com.github.vitorpereiraa.sombra.domain.http.HttpResponse;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class ReportingService {

    private final SombraMetrics metrics;
    private final ExchangeLogger logger;
    private final boolean metricsEnabled;
    private final boolean loggingEnabled;

    public ReportingService(ReportingProperties properties, SombraMetrics metrics, ExchangeLogger logger) {
        this.metrics = metrics;
        this.logger = logger;
        this.metricsEnabled = properties.metrics().enabled();
        this.loggingEnabled = properties.logging().enabled();
    }

    public void reportSuccess(
            CapturedExchange exchange,
            HttpResponse candidate,
            ComparisonResult result,
            Duration replayDuration,
            Duration comparisonDuration) {

        if (metricsEnabled) {
            var method = exchange.request().method().name();
            var originalStatusClass = ReportingTags.statusClass(exchange.response().statusCode());
            var candidateStatusClass = ReportingTags.statusClass(candidate.statusCode());
            var outcome = result.matched() ? "match" : "mismatch";

            metrics.recordOriginalDuration(exchange.response().duration(), method, originalStatusClass);
            metrics.recordReplayDuration(replayDuration, method, candidateStatusClass, outcome);
            metrics.recordComparisonDuration(comparisonDuration);
            metrics.recordProcessed(outcome, method, candidateStatusClass);
            for (var discrepancy : result.discrepancies()) {
                metrics.recordDiscrepancy(
                        discrepancy.getClass().getSimpleName(), ReportingTags.fieldKind(discrepancy.field()));
            }
        }

        if (loggingEnabled) {
            logger.log(exchange, candidate, result, replayDuration, null);
        }
    }

    public void reportError(CapturedExchange exchange, Duration replayDuration, String errorKind, Throwable error) {
        if (metricsEnabled) {
            var method = exchange.request().method().name();
            metrics.recordOriginalDuration(
                    exchange.response().duration(), method, ReportingTags.statusClass(exchange.response().statusCode()));
            metrics.recordReplayError(errorKind);
            metrics.recordProcessed("error", method, ReportingTags.STATUS_CLASS_NONE);
            if (replayDuration != null) {
                metrics.recordReplayDuration(
                        replayDuration, method, ReportingTags.STATUS_CLASS_NONE, "error");
            }
        }

        if (loggingEnabled) {
            logger.log(exchange, null, null, replayDuration, error);
        }
    }
}
