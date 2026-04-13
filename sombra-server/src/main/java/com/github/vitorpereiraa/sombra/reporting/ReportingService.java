package com.github.vitorpereiraa.sombra.reporting;

import com.github.vitorpereiraa.sombra.domain.capture.CapturedExchange;
import com.github.vitorpereiraa.sombra.domain.comparison.ComparisonResult;
import com.github.vitorpereiraa.sombra.domain.http.HttpResponse;
import com.github.vitorpereiraa.sombra.domain.http.StatusCode;
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
            var originalStatusClass = statusClass(exchange.response().statusCode());
            var candidateStatusClass = statusClass(candidate.statusCode());
            var outcome = result.matched() ? "match" : "mismatch";

            metrics.recordOriginalDuration(exchange.response().duration(), method, originalStatusClass);
            metrics.recordReplayDuration(replayDuration, method, candidateStatusClass, outcome);
            metrics.recordComparisonDuration(comparisonDuration);
            metrics.recordProcessed(outcome, method, candidateStatusClass);
            for (var discrepancy : result.discrepancies()) {
                var reported = ReportedDiscrepancy.from(discrepancy);
                metrics.recordDiscrepancy(reported.type(), reported.fieldKind());
            }
        }

        if (loggingEnabled) {
            logger.logComparison(exchange, candidate, result, replayDuration);
        }
    }

    public void reportError(CapturedExchange exchange, Duration replayDuration, Throwable error) {
        if (metricsEnabled) {
            var method = exchange.request().method().name();
            metrics.recordOriginalDuration(
                    exchange.response().duration(), method, statusClass(exchange.response().statusCode()));
            metrics.recordReplayError();
            metrics.recordReplayErrorDuration(replayDuration, method);
        }

        if (loggingEnabled) {
            logger.logReplayError(exchange, replayDuration, error);
        }
    }

    private static String statusClass(StatusCode statusCode) {
        return (statusCode.value() / 100) + "xx";
    }
}
