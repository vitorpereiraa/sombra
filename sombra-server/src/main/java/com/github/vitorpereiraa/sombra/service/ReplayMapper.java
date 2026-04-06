package com.github.vitorpereiraa.sombra.service;

import com.github.vitorpereiraa.sombra.domain.HttpBody;
import com.github.vitorpereiraa.sombra.domain.HttpHeader;
import com.github.vitorpereiraa.sombra.domain.HttpResponse;
import com.github.vitorpereiraa.sombra.domain.StatusCode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

public final class ReplayMapper {

    private static final Set<String> EXCLUDED_HEADERS =
            Set.of("host", "content-length", "transfer-encoding", "connection");

    public static HttpHeaders toSpringHeaders(List<HttpHeader> headers) {
        var springHeaders = new HttpHeaders();
        headers.stream()
                .filter(header -> !EXCLUDED_HEADERS.contains(header.name().toLowerCase()))
                .forEach(header -> springHeaders.addAll(header.name(), header.values()));
        return springHeaders;
    }

    public static HttpResponse toDomain(ClientHttpResponse response) throws IOException {
        var statusCode = new StatusCode(response.getStatusCode().value());

        var headers = response.getHeaders().headerNames().stream()
                .filter(name -> !response.getHeaders().getValuesAsList(name).isEmpty())
                .map(name -> new HttpHeader(name, response.getHeaders().getValuesAsList(name)))
                .toList();

        var bodyStr = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
        var body = HttpBody.of(bodyStr);

        return new HttpResponse(statusCode, headers, body);
    }
}
