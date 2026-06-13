package com.github.vitorpereiraa.sombra.orchestrator;

import com.github.vitorpereiraa.sombra.comparison.ComparisonService;
import com.github.vitorpereiraa.sombra.domain.capture.CapturedExchange;
import com.github.vitorpereiraa.sombra.domain.http.HttpResponse;
import com.github.vitorpereiraa.sombra.replay.ReplayService;
import com.github.vitorpereiraa.sombra.reporting.ReportingService;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
public class Orchestrator {

    private final ReplayService replayService;
    private final ComparisonService comparisonService;
    private final ReportingService reportingService;

    public Orchestrator(
            ReplayService replayService,
            ComparisonService comparisonService,
            ReportingService reportingService) {
        this.replayService = replayService;
        this.comparisonService = comparisonService;
        this.reportingService = reportingService;
    }

    public void process(CapturedExchange exchange) {
        HttpResponse replayedResponse;
        try {
            replayedResponse = replayService.replay(exchange.request());
        } catch (RestClientException e) {
            reportingService.reportError(exchange, e);
            return;
        }

        var result = comparisonService.compare(exchange.response(), replayedResponse);

        reportingService.reportSuccess(exchange, result);
    }
}
