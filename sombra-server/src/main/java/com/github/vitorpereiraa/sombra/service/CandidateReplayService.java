package com.github.vitorpereiraa.sombra.service;

import com.github.vitorpereiraa.sombra.domain.HttpBody;
import com.github.vitorpereiraa.sombra.domain.HttpHeader;
import com.github.vitorpereiraa.sombra.domain.HttpRequest;
import com.github.vitorpereiraa.sombra.domain.HttpResponse;
import com.github.vitorpereiraa.sombra.domain.StatusCode;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

@Component
public class CandidateReplayService {

    private static final Set<String> EXCLUDED_HEADERS =
            Set.of("host", "content-length", "transfer-encoding", "connection");

    private final RestClient restClient;

    public CandidateReplayService(RestClient restClient) {
        this.restClient = restClient;
    }

    public HttpResponse replay(HttpRequest request) {
        var method = HttpMethod.valueOf(request.method().name());

        var spec = restClient
                .method(method)
                .uri(request.path().value())
                .header("X-Sombra-Replay", "true")
                .headers(h -> request.headers().stream()
                        .filter(header ->
                                !EXCLUDED_HEADERS.contains(header.name().toLowerCase()))
                        .forEach(header -> h.addAll(header.name(), header.values())));

        request.body().ifPresent(body -> spec.body(body.content()));

        return spec.exchange((req, res) -> {
            var statusCode = new StatusCode(res.getStatusCode().value());

            var headers = res.getHeaders().headerNames().stream()
                    .filter(name -> !res.getHeaders().getValuesAsList(name).isEmpty())
                    .map(name -> new HttpHeader(name, res.getHeaders().getValuesAsList(name)))
                    .toList();

            var bodyStr = StreamUtils.copyToString(res.getBody(), StandardCharsets.UTF_8);
            var body = HttpBody.of(bodyStr);

            return new HttpResponse(statusCode, headers, body);
        });
    }
}
