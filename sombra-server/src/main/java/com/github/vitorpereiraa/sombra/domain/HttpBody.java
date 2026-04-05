package com.github.vitorpereiraa.sombra.domain;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public record HttpBody(String content) {

    public HttpBody {
        checkNotNull(content, "HttpBody content cannot be null");
        checkArgument(!content.isBlank(), "HttpBody content cannot be blank");
    }

    public static Optional<HttpBody> of(String content) {
        if (content == null || content.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new HttpBody(content));
    }
}
