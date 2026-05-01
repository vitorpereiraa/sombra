package com.github.vitorpereiraa.sombra.agent.service;

import com.github.vitorpereiraa.sombra.agent.streaming.CapturedExchangeEventMapper;
import com.github.vitorpereiraa.sombra.agent.streaming.CapturedExchangeProducer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

public class CaptureFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CaptureFilter.class);

    private final CapturedExchangeProducer producer;

    public CaptureFilter(CapturedExchangeProducer producer) {
        this.producer = producer;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getHeader("X-Sombra-Replay") != null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var wrappedRequest = new ContentCachingRequestWrapper(request, Math.max(request.getContentLength(), 1024));
        var wrappedResponse = new ContentCachingResponseWrapper(response);

        long startNs = System.nanoTime();
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            byte[] responseBody = wrappedResponse.getContentAsByteArray();
            wrappedResponse.copyBodyToResponse();
            long durationNs = System.nanoTime() - startNs;
            capture(wrappedRequest, wrappedResponse, responseBody, durationNs);
        }
    }

    private void capture(
            ContentCachingRequestWrapper request,
            ContentCachingResponseWrapper response,
            byte[] responseBody,
            long durationNs) {
        try {
            producer.send(CapturedExchangeEventMapper.toEvent(request, response, responseBody, durationNs));
        } catch (Exception e) {
            log.error("Failed to capture exchange", e);
        }
    }
}
