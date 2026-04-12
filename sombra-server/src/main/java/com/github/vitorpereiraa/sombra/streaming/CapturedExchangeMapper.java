package com.github.vitorpereiraa.sombra.streaming;

import com.github.vitorpereiraa.sombra.agent.streaming.dto.CapturedExchangeEvent;
import com.github.vitorpereiraa.sombra.domain.capture.CapturedExchange;
import com.github.vitorpereiraa.sombra.domain.capture.TraceId;
import com.github.vitorpereiraa.sombra.domain.http.HttpBody;
import com.github.vitorpereiraa.sombra.domain.http.HttpHeader;
import com.github.vitorpereiraa.sombra.domain.http.HttpMethod;
import com.github.vitorpereiraa.sombra.domain.http.HttpRequest;
import com.github.vitorpereiraa.sombra.domain.http.HttpResponse;
import com.github.vitorpereiraa.sombra.domain.http.RequestPath;
import com.github.vitorpereiraa.sombra.domain.http.StatusCode;

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
