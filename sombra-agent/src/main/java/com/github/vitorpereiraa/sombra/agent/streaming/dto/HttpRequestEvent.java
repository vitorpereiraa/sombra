package com.github.vitorpereiraa.sombra.agent.streaming.dto;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record HttpRequestEvent(
    String method,
    String path,
    Map<String, List<String>> headers,
    Optional<String> body
) {}
