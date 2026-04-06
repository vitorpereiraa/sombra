package com.github.vitorpereiraa.sombra.streaming;

import com.github.vitorpereiraa.sombra.agent.streaming.dto.CapturedExchangeEvent;
import com.github.vitorpereiraa.sombra.agent.streaming.dto.HttpRequestEvent;
import com.github.vitorpereiraa.sombra.agent.streaming.dto.HttpResponseEvent;
import com.github.vitorpereiraa.sombra.domain.CapturedExchange;
import com.github.vitorpereiraa.sombra.domain.HttpBody;
import com.github.vitorpereiraa.sombra.domain.HttpHeader;
import com.github.vitorpereiraa.sombra.domain.HttpMethod;
import com.github.vitorpereiraa.sombra.domain.HttpRequest;
import com.github.vitorpereiraa.sombra.domain.HttpResponse;
import com.github.vitorpereiraa.sombra.domain.RequestPath;
import com.github.vitorpereiraa.sombra.domain.StatusCode;
import com.github.vitorpereiraa.sombra.domain.TraceId;

import java.util.List;
import java.util.Map;

public final class CapturedExchangeMapper {

    public static CapturedExchange toDomain(CapturedExchangeEvent event) {
        var request = new HttpRequest(
            HttpMethod.from(event.request().method()),
            new RequestPath(event.request().path()),
            toHeaders(event.request().headers()),
            event.request().body().flatMap(HttpBody::of)
        );

        var response = new HttpResponse(
            new StatusCode(event.response().statusCode()),
            toHeaders(event.response().headers()),
            event.response().body().flatMap(HttpBody::of)
        );

        return new CapturedExchange(
            request,
            response,
            event.timestamp(),
            event.traceId().map(TraceId::new)
        );
    }

    private static List<HttpHeader> toHeaders(Map<String, List<String>> raw) {
        return raw.entrySet().stream()
            .map(entry -> new HttpHeader(entry.getKey(), entry.getValue()))
            .toList();
    }
}
