package com.github.vitorpereiraa.sombra.service;

import com.github.vitorpereiraa.sombra.config.ReportingProperties;
import com.github.vitorpereiraa.sombra.domain.capture.CapturedExchange;
import com.github.vitorpereiraa.sombra.domain.comparison.ComparisonResult;
import com.github.vitorpereiraa.sombra.domain.reporting.ReportedDiscrepancy;
import org.springframework.stereotype.Component;

@Component
public class ReportingService {

    private final SombraMetrics metrics;
    private final ExchangeLogger logger;
    private final boolean loggingEnabled;

    public ReportingService(ReportingProperties properties, SombraMetrics metrics, ExchangeLogger logger) {
        this.metrics = metrics;
        this.logger = logger;
        this.loggingEnabled = properties.logging().enabled();
    }

    public void reportSuccess(CapturedExchange exchange, ComparisonResult result) {
        var method = exchange.request().method();
        var outcome = result.matched() ? "match" : "mismatch";

        metrics.recordOriginalDuration(
                method,
                result.originalResponse().duration(),
                result.originalResponse().statusCode());

        metrics.recordReplayDuration(
                method,
                result.candidateResponse().duration(),
                result.candidateResponse().statusCode(),
                outcome);

        metrics.recordProcessed(outcome, method, result.candidateResponse().statusCode());

        for (var discrepancy : result.discrepancies()) {
            var reported = ReportedDiscrepancy.from(discrepancy);
            metrics.recordDiscrepancy(reported.type(), reported.fieldKind());
        }

        if (loggingEnabled) {
            logger.logComparison(exchange, result);
        }
    }

    public void reportError(CapturedExchange exchange, Throwable error) {
        metrics.recordOriginalDuration(
                exchange.request().method(),
                exchange.response().duration(),
                exchange.response().statusCode());
        metrics.recordReplayError();

        if (loggingEnabled) {
            logger.logReplayError(exchange, error);
        }
    }
}
