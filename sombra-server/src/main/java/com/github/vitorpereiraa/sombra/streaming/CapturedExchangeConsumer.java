package com.github.vitorpereiraa.sombra.streaming;

import com.github.vitorpereiraa.sombra.agent.streaming.dto.CapturedExchangeEvent;
import com.github.vitorpereiraa.sombra.domain.http.HttpResponse;
import com.github.vitorpereiraa.sombra.service.ReportingService;
import com.github.vitorpereiraa.sombra.service.CandidateReplayService;
import com.github.vitorpereiraa.sombra.service.ResponseComparisonService;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
public class CapturedExchangeConsumer {

    private static final Logger log = LoggerFactory.getLogger(CapturedExchangeConsumer.class);

    private final CandidateReplayService replayService;
    private final ResponseComparisonService comparisonService;
    private final ReportingService reportingService;

    public CapturedExchangeConsumer(
            CandidateReplayService replayService,
            ResponseComparisonService comparisonService,
            ReportingService reportingService) {
        this.replayService = replayService;
        this.comparisonService = comparisonService;
        this.reportingService = reportingService;
    }

    @KafkaListener(topics = "${sombra.server.topic-name}")
    public void consume(CapturedExchangeEvent event) {
        var exchange = CapturedExchangeMapper.toDomain(event);
        log.debug(
                "Received captured exchange: {} {} -> {}",
                exchange.request().method(),
                exchange.request().path().value(),
                exchange.response().statusCode().value());

        long replayStartNs = System.nanoTime();
        HttpResponse replayedResponse;
        try {
            replayedResponse = replayService.replay(exchange.request());
        } catch (RestClientException e) {
            var replayDuration = Duration.ofNanos(System.nanoTime() - replayStartNs);
            reportingService.reportError(exchange, replayDuration, e);
            log.error(
                    "Failed to replay exchange: {} {}",
                    exchange.request().method(),
                    exchange.request().path().value(),
                    e);
            return;
        }

        long comparisonStartNs = System.nanoTime();
        var result = comparisonService.compare(exchange.response(), replayedResponse);
        var comparisonDuration = Duration.ofNanos(System.nanoTime() - comparisonStartNs);

        reportingService.reportSuccess(
                exchange, replayedResponse, result, replayedResponse.duration(), comparisonDuration);
    }
}
