package com.github.vitorpereiraa.sombra.agent.domain;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record CapturedExchange(
    String method,
    String path,
    Map<String, List<String>> requestHeaders,
    String requestBody,
    int statusCode,
    Map<String, List<String>> responseHeaders,
    String responseBody,
    Instant timestamp,
    String traceId
) {

    public static CapturedExchange of(
            String method,
            String path,
            Map<String, List<String>> requestHeaders,
            String requestBody,
            int statusCode,
            Map<String, List<String>> responseHeaders,
            String responseBody,
            String traceId) {
        return new CapturedExchange(
            method, path, requestHeaders, requestBody,
            statusCode, responseHeaders, responseBody,
            Instant.now(), traceId
        );
    }
}
