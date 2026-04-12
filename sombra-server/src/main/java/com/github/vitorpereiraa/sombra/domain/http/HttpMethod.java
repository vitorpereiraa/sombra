package com.github.vitorpereiraa.sombra.domain.http;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum HttpMethod {
    GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS;

    private static final Map<String, HttpMethod> BY_NAME = Stream.of(values())
        .collect(Collectors.toUnmodifiableMap(m -> m.name().toUpperCase(), m -> m));

    public static HttpMethod from(String value) {
        return of(value).orElseThrow(() ->
            new IllegalArgumentException("Unknown HTTP method: " + value));
    }

    public static Optional<HttpMethod> of(String value) {
        if (value == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_NAME.get(value.toUpperCase()));
    }
}
