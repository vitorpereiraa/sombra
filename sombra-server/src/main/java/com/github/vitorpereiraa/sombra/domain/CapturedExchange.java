package com.github.vitorpereiraa.sombra.domain;

import java.time.Instant;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public record CapturedExchange(
    HttpRequest request,
    HttpResponse response,
    Instant timestamp,
    Optional<TraceId> traceId
) {

    public CapturedExchange {
        checkArgument(request != null, "CapturedExchange request cannot be null");
        checkArgument(response != null, "CapturedExchange response cannot be null");
        checkArgument(timestamp != null, "CapturedExchange timestamp cannot be null");
        checkNotNull(traceId, "CapturedExchange traceId Optional cannot be null");
    }
}
