package com.github.vitorpereiraa.sombra.agent.domain;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public record HttpBody(String content) {

    public HttpBody {
        checkNotNull(content, "HttpBody content cannot be null");
        checkArgument(!content.isBlank(), "HttpBody content cannot be blank");
    }

    public static Optional<HttpBody> of(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return Optional.empty();
        }
        var content = new String(bytes, StandardCharsets.UTF_8);
        if (content.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new HttpBody(content));
    }
}
