package com.github.vitorpereiraa.sombra.agent.streaming.dto;

import java.util.List;
import java.util.Map;

public record HttpResponseEvent(
    int statusCode,
    Map<String, List<String>> headers,
    String body
) {}
