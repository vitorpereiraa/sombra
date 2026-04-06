package com.github.vitorpereiraa.sombra.domain;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

public record HttpBody(String content) {

    public HttpBody {
        checkArgument(content != null, "HttpBody content cannot be null");
        checkArgument(!content.isBlank(), "HttpBody content cannot be blank");
    }

    public static Optional<HttpBody> of(String content) {
        try {
            return Optional.of(new HttpBody(content));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
