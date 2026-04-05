package com.github.vitorpereiraa.sombra.agent.domain;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public record HttpRequest(
    HttpMethod method,
    RequestPath path,
    HttpHeaders headers,
    Optional<HttpBody> body
) {

    public HttpRequest {
        checkNotNull(method, "HttpRequest method cannot be null");
        checkNotNull(path, "HttpRequest path cannot be null");
        checkNotNull(headers, "HttpRequest headers cannot be null");
        checkNotNull(body, "HttpRequest body Optional cannot be null");
    }
}
