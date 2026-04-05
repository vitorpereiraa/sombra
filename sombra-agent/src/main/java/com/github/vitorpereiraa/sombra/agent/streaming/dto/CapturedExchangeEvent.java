package com.github.vitorpereiraa.sombra.agent.streaming.dto;

import java.time.Instant;
import java.util.Optional;

public record CapturedExchangeEvent(
    HttpRequestEvent request,
    HttpResponseEvent response,
    Instant timestamp,
    Optional<String> traceId
) {}
