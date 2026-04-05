package com.github.vitorpereiraa.sombra.domain;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public record HttpHeaders(Map<HeaderName, List<HeaderValue>> entries) {

    public HttpHeaders {
        checkNotNull(entries, "HttpHeaders entries cannot be null");
        entries = Map.copyOf(entries);
    }

    public static HttpHeaders from(Map<String, List<String>> raw) {
        checkNotNull(raw, "raw headers map cannot be null");
        Map<HeaderName, List<HeaderValue>> entries = new LinkedHashMap<>();
        raw.forEach((name, values) ->
            entries.put(
                new HeaderName(name),
                values.stream().map(HeaderValue::new).toList()
            )
        );
        return new HttpHeaders(entries);
    }

    public Map<String, List<String>> toRawMap() {
        Map<String, List<String>> raw = new LinkedHashMap<>();
        entries.forEach((name, values) ->
            raw.put(name.value(), values.stream().map(HeaderValue::value).toList())
        );
        return raw;
    }
}
