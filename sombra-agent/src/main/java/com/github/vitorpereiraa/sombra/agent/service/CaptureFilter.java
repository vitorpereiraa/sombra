package com.github.vitorpereiraa.sombra.agent.service;

import com.github.vitorpereiraa.sombra.agent.domain.CapturedExchange;
import com.github.vitorpereiraa.sombra.agent.streaming.CapturedExchangeProducer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CaptureFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CaptureFilter.class);

    private final CapturedExchangeProducer producer;

    public CaptureFilter(CapturedExchangeProducer producer) {
        this.producer = producer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        int contentLength = request.getContentLength() > 0 ? request.getContentLength() : 1024;
        var wrappedRequest = new ContentCachingRequestWrapper(request, contentLength);
        var wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            try {
                captureAndSend(wrappedRequest, wrappedResponse);
            } catch (Exception e) {
                log.error("Failed to capture exchange", e);
            }
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void captureAndSend(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        String path = request.getRequestURI();
        String queryString = request.getQueryString();
        if (queryString != null) {
            path = path + "?" + queryString;
        }

        var exchange = CapturedExchange.of(
            request.getMethod(),
            path,
            extractHeaders(request),
            toStringOrNull(request.getContentAsByteArray()),
            response.getStatus(),
            extractResponseHeaders(response),
            toStringOrNull(response.getContentAsByteArray()),
            null
        );

        producer.send(exchange);
    }

    private Map<String, List<String>> extractHeaders(HttpServletRequest request) {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        for (String name : Collections.list(request.getHeaderNames())) {
            headers.put(name, Collections.list(request.getHeaders(name)));
        }
        return headers;
    }

    private Map<String, List<String>> extractResponseHeaders(HttpServletResponse response) {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        for (String name : response.getHeaderNames()) {
            headers.put(name, List.copyOf(response.getHeaders(name)));
        }
        return headers;
    }

    private String toStringOrNull(byte[] content) {
        if (content == null || content.length == 0) {
            return null;
        }
        return new String(content, StandardCharsets.UTF_8);
    }
}
