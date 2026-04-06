package com.github.vitorpereiraa.sombra.domain;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public record HttpResponse(
    StatusCode statusCode,
    List<HttpHeader> headers,
    Optional<HttpBody> body
) {

    public HttpResponse {
        checkArgument(statusCode != null, "HttpResponse statusCode cannot be null");
        checkArgument(headers != null, "HttpResponse headers cannot be null");
        checkNotNull(body, "HttpResponse body Optional cannot be null");
        headers = List.copyOf(headers);
    }
}
