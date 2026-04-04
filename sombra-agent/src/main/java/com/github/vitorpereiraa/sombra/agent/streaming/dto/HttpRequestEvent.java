package com.github.vitorpereiraa.sombra.agent.streaming.dto;

import java.util.List;
import java.util.Map;

public record HttpRequestEvent(
    String method,
    String path,
    Map<String, List<String>> headers,
    String body
) {}
