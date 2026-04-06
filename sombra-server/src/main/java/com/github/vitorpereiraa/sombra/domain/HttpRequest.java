package com.github.vitorpereiraa.sombra.domain;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public record HttpRequest(
    HttpMethod method,
    RequestPath path,
    List<HttpHeader> headers,
    Optional<HttpBody> body
) {

    public HttpRequest {
        checkArgument(method != null, "HttpRequest method cannot be null");
        checkArgument(path != null, "HttpRequest path cannot be null");
        checkArgument(headers != null, "HttpRequest headers cannot be null");
        checkNotNull(body, "HttpRequest body Optional cannot be null");
        headers = List.copyOf(headers);
    }
}
