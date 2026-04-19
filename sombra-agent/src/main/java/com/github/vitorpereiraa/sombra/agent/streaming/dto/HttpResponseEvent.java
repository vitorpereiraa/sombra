package com.github.vitorpereiraa.sombra.agent.streaming.dto;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record HttpResponseEvent(
    int statusCode,
    Map<String, List<String>> headers,
    Optional<String> body,
    long durationMs
) {}
