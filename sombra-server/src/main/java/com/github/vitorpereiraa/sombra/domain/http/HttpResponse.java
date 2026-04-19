package com.github.vitorpereiraa.sombra.domain.http;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public record HttpResponse(
    StatusCode statusCode,
    List<HttpHeader> headers,
    Optional<HttpBody> body,
    Duration duration
) {

    public HttpResponse {
        checkArgument(statusCode != null, "HttpResponse statusCode cannot be null");
        checkArgument(headers != null, "HttpResponse headers cannot be null");
        checkNotNull(body, "HttpResponse body Optional cannot be null");
        checkArgument(duration != null, "HttpResponse duration cannot be null");
        checkArgument(!duration.isNegative(), "HttpResponse duration cannot be negative");
        headers = List.copyOf(headers);
    }
}
