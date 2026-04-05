package com.github.vitorpereiraa.sombra.agent.domain;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum HttpMethod {
    GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS;

    private static final Map<String, HttpMethod> BY_NAME = Arrays.stream(values())
        .collect(Collectors.toUnmodifiableMap(m -> m.name().toUpperCase(), Function.identity()));

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
