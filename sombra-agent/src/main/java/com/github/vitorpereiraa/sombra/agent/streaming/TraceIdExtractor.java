package com.github.vitorpereiraa.sombra.agent.streaming;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

final class TraceIdExtractor {

    private static final String TRACEPARENT_HEADER = "traceparent";
    private static final String X_REQUEST_ID_HEADER = "X-Request-Id";
    private static final Pattern TRACEPARENT_PATTERN =
            Pattern.compile("^00-([0-9a-f]{32})-[0-9a-f]{16}-[0-9a-f]{2}$");
    private static final String ZERO_TRACE_ID = "0".repeat(32);
    private static final int MAX_REQUEST_ID_LEN = 128;

    private TraceIdExtractor() {}

    static String extract(HttpServletRequest request) {
        return fromTraceparent(request.getHeader(TRACEPARENT_HEADER))
                .or(() -> fromRequestId(request.getHeader(X_REQUEST_ID_HEADER)))
                .orElseGet(() -> UUID.randomUUID().toString());
    }

    private static Optional<String> fromTraceparent(String value) {
        if (value == null) {
            return Optional.empty();
        }
        var matcher = TRACEPARENT_PATTERN.matcher(value);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        var traceId = matcher.group(1);
        return ZERO_TRACE_ID.equals(traceId) ? Optional.empty() : Optional.of(traceId);
    }

    private static Optional<String> fromRequestId(String value) {
        if (value == null || value.isBlank() || value.length() > MAX_REQUEST_ID_LEN) {
            return Optional.empty();
        }
        return Optional.of(value);
    }
}
