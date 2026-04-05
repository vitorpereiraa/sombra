package com.github.vitorpereiraa.sombra.streaming;

import com.github.vitorpereiraa.sombra.agent.streaming.dto.CapturedExchangeEvent;
import com.github.vitorpereiraa.sombra.domain.CapturedExchange;
import com.github.vitorpereiraa.sombra.domain.HttpBody;
import com.github.vitorpereiraa.sombra.domain.HttpHeaders;
import com.github.vitorpereiraa.sombra.domain.HttpMethod;
import com.github.vitorpereiraa.sombra.domain.HttpRequest;
import com.github.vitorpereiraa.sombra.domain.HttpResponse;
import com.github.vitorpereiraa.sombra.domain.RequestPath;
import com.github.vitorpereiraa.sombra.domain.StatusCode;
import com.github.vitorpereiraa.sombra.domain.TraceId;

import static com.google.common.base.Preconditions.checkNotNull;

public final class CapturedExchangeMapper {

    public static CapturedExchange toDomain(CapturedExchangeEvent event) {
        checkNotNull(event, "CapturedExchangeEvent cannot be null");

        var request = new HttpRequest(
            HttpMethod.from(event.request().method()),
            new RequestPath(event.request().path()),
            HttpHeaders.from(event.request().headers()),
            event.request().body().flatMap(HttpBody::of)
        );

        var response = new HttpResponse(
            new StatusCode(event.response().statusCode()),
            HttpHeaders.from(event.response().headers()),
            event.response().body().flatMap(HttpBody::of)
        );

        return new CapturedExchange(
            request,
            response,
            event.timestamp(),
            event.traceId().map(TraceId::new)
        );
    }
}
