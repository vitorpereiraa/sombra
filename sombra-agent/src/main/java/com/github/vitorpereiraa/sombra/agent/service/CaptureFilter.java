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
                var exchange = CapturedExchange.from(wrappedRequest, wrappedResponse);
                producer.send(exchange);
            } catch (Exception e) {
                log.error("Failed to capture exchange", e);
            }
            wrappedResponse.copyBodyToResponse();
        }
    }
}
