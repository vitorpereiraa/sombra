package com.github.vitorpereiraa.sombra.service;

import com.github.vitorpereiraa.sombra.domain.capture.CapturedExchange;
import com.github.vitorpereiraa.sombra.domain.comparison.ComparisonResult;
import com.github.vitorpereiraa.sombra.domain.reporting.ReportedDiscrepancy;
import org.springframework.stereotype.Component;

@Component
public class ReportingService {

    private final DiscrepancyMetrics metrics;
    private final DiscrepancyLogger logger;

    public ReportingService(DiscrepancyMetrics metrics, DiscrepancyLogger logger) {
        this.metrics = metrics;
        this.logger = logger;
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
            metrics.recordDiscrepancy(
                    ReportedDiscrepancy.typeOf(discrepancy),
                    ReportedDiscrepancy.fieldKindOf(discrepancy.field()));
        }

        logger.logComparison(exchange, result);
    }

    public void reportError(CapturedExchange exchange, Throwable error) {
        metrics.recordOriginalDuration(
                exchange.request().method(),
                exchange.response().duration(),
                exchange.response().statusCode());
        metrics.recordReplayError(error.getClass().getSimpleName());

        logger.logReplayError(exchange, error);
    }
}
