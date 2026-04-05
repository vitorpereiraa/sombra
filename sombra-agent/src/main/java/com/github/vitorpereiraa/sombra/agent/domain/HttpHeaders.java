package com.github.vitorpereiraa.sombra.agent.domain;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public record HttpHeaders(Map<HeaderName, List<String>> entries) {

    public HttpHeaders {
        checkNotNull(entries, "HttpHeaders entries cannot be null");
        entries = Map.copyOf(entries);
    }

    public static HttpHeaders fromServletRequest(HttpServletRequest request) {
        Map<HeaderName, List<String>> headers = new LinkedHashMap<>();
        for (String name : Collections.list(request.getHeaderNames())) {
            headers.put(new HeaderName(name), Collections.list(request.getHeaders(name)));
        }
        return new HttpHeaders(headers);
    }

    public static HttpHeaders fromServletResponse(HttpServletResponse response) {
        Map<HeaderName, List<String>> headers = new LinkedHashMap<>();
        for (String name : response.getHeaderNames()) {
            headers.put(new HeaderName(name), List.copyOf(response.getHeaders(name)));
        }
        return new HttpHeaders(headers);
    }

    public Map<String, List<String>> toRawMap() {
        Map<String, List<String>> raw = new LinkedHashMap<>();
        entries.forEach((name, values) -> raw.put(name.value(), values));
        return raw;
    }
}
