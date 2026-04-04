package com.github.vitorpereiraa.sombra.agent.streaming.dto;

import java.time.Instant;

public record CapturedExchangeEvent(
    HttpRequestEvent request,
    HttpResponseEvent response,
    Instant timestamp,
    String traceId
) {}
