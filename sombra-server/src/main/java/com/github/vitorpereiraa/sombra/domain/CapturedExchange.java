package com.github.vitorpereiraa.sombra.domain;

import java.time.Instant;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public record CapturedExchange(
    HttpRequest request,
    HttpResponse response,
    Instant timestamp,
    Optional<TraceId> traceId
) {

    public CapturedExchange {
        checkNotNull(request, "CapturedExchange request cannot be null");
        checkNotNull(response, "CapturedExchange response cannot be null");
        checkNotNull(timestamp, "CapturedExchange timestamp cannot be null");
        checkNotNull(traceId, "CapturedExchange traceId Optional cannot be null");
    }
}
