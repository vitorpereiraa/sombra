package com.github.vitorpereiraa.sombra.domain;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public record HttpResponse(
    StatusCode statusCode,
    HttpHeaders headers,
    Optional<HttpBody> body
) {

    public HttpResponse {
        checkNotNull(statusCode, "HttpResponse statusCode cannot be null");
        checkNotNull(headers, "HttpResponse headers cannot be null");
        checkNotNull(body, "HttpResponse body Optional cannot be null");
    }
}
