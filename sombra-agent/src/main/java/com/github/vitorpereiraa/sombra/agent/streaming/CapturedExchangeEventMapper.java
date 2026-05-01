package com.github.vitorpereiraa.sombra.agent.streaming;

import com.github.vitorpereiraa.sombra.agent.streaming.dto.CapturedExchangeEvent;
import com.github.vitorpereiraa.sombra.agent.streaming.dto.HttpRequestEvent;
import com.github.vitorpereiraa.sombra.agent.streaming.dto.HttpResponseEvent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class CapturedExchangeEventMapper {

    private static final String TRACEPARENT_HEADER = "traceparent";
    private static final String X_REQUEST_ID_HEADER = "X-Request-Id";

    public static CapturedExchangeEvent toEvent(
        ContentCachingRequestWrapper request,
        ContentCachingResponseWrapper response,
        byte[] responseBody,
        long durationNs
    ) {

        var requestEvent = new HttpRequestEvent(
            request.getMethod(),
            buildPath(request),
            extractRequestHeaders(request),
            extractBody(request.getContentAsByteArray())
        );

        var responseEvent = new HttpResponseEvent(
            response.getStatus(),
            extractResponseHeaders(response),
            extractBody(responseBody),
            durationNs
        );

        return new CapturedExchangeEvent(
            requestEvent,
            responseEvent,
            Instant.now(),
            Optional.of(extractTraceId(request))
        );
    }

    private static String extractTraceId(HttpServletRequest request) {
        var traceparent = request.getHeader(TRACEPARENT_HEADER);
        if (traceparent != null) {
            var parts = traceparent.split("-");
            if (parts.length >= 2 && parts[1].length() == 32) {
                return parts[1];
            }
        }
        var requestId = request.getHeader(X_REQUEST_ID_HEADER);
        if (requestId != null && !requestId.isBlank()) {
            return requestId;
        }
        return UUID.randomUUID().toString();
    }

    private static String buildPath(HttpServletRequest request) {
        var uri = request.getRequestURI();
        var queryString = request.getQueryString();
        return queryString != null ? uri + "?" + queryString : uri;
    }

    private static Map<String, List<String>> extractRequestHeaders(HttpServletRequest request) {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        for (String name : Collections.list(request.getHeaderNames())) {
            headers.put(name, Collections.list(request.getHeaders(name)));
        }
        return headers;
    }

    private static Map<String, List<String>> extractResponseHeaders(HttpServletResponse response) {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        for (String name : response.getHeaderNames()) {
            headers.put(name, List.copyOf(response.getHeaders(name)));
        }
        return headers;
    }

    private static Optional<String> extractBody(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return Optional.empty();
        }
        var content = new String(bytes, StandardCharsets.UTF_8);
        return content.isBlank() ? Optional.empty() : Optional.of(content);
    }
}
