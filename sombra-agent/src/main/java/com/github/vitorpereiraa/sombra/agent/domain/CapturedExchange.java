package com.github.vitorpereiraa.sombra.agent.domain;

import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.time.Instant;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public record CapturedExchange(
    HttpRequest request,
    HttpResponse response,
    Instant timestamp,
    Optional<TraceId> traceId
) {

    public CapturedExchange {
        checkNotNull(request, "CapturedExchange request cannot be null");
        checkNotNull(response, "CapturedExchange response cannot be null");
        checkNotNull(timestamp, "CapturedExchange timestamp cannot be null");
        checkNotNull(traceId, "CapturedExchange traceId Optional cannot be null");
    }

    public static CapturedExchange from(
            ContentCachingRequestWrapper servletRequest,
            ContentCachingResponseWrapper servletResponse) {

        var method = HttpMethod.from(servletRequest.getMethod());
        var path = RequestPath.from(servletRequest.getRequestURI(), servletRequest.getQueryString());
        var requestHeaders = HttpHeaders.fromServletRequest(servletRequest);
        var requestBody = HttpBody.of(servletRequest.getContentAsByteArray());

        var statusCode = new StatusCode(servletResponse.getStatus());
        var responseHeaders = HttpHeaders.fromServletResponse(servletResponse);
        var responseBody = HttpBody.of(servletResponse.getContentAsByteArray());

        var request = new HttpRequest(method, path, requestHeaders, requestBody);
        var response = new HttpResponse(statusCode, responseHeaders, responseBody);

        return new CapturedExchange(request, response, Instant.now(), Optional.empty());
    }
}
