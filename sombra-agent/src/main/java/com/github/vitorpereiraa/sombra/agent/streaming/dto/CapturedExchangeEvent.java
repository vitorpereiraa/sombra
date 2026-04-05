package com.github.vitorpereiraa.sombra.agent.streaming.dto;

import com.github.vitorpereiraa.sombra.agent.domain.CapturedExchange;
import com.github.vitorpereiraa.sombra.agent.domain.HttpBody;
import com.github.vitorpereiraa.sombra.agent.domain.TraceId;

import java.time.Instant;
import java.util.Optional;

public record CapturedExchangeEvent(
    HttpRequestEvent request,
    HttpResponseEvent response,
    Instant timestamp,
    Optional<String> traceId
) {

    public static CapturedExchangeEvent from(CapturedExchange exchange) {
        var req = exchange.request();
        var res = exchange.response();

        var requestEvent = new HttpRequestEvent(
            req.method().name(),
            req.path().value(),
            req.headers().toRawMap(),
            req.body().map(HttpBody::content)
        );

        var responseEvent = new HttpResponseEvent(
            res.statusCode().value(),
            res.headers().toRawMap(),
            res.body().map(HttpBody::content)
        );

        return new CapturedExchangeEvent(
            requestEvent,
            responseEvent,
            exchange.timestamp(),
            exchange.traceId().map(TraceId::value)
        );
    }
}
