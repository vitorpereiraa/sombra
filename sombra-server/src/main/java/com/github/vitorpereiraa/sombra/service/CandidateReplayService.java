package com.github.vitorpereiraa.sombra.service;

import com.github.vitorpereiraa.sombra.domain.http.HttpRequest;
import com.github.vitorpereiraa.sombra.domain.http.HttpResponse;
import java.time.Duration;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CandidateReplayService {

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
                .headers(h -> h.addAll(ReplayMapper.toSpringHeaders(request.headers())));

        request.body().ifPresent(body -> spec.body(body.content()));

        long startNs = System.nanoTime();
        return spec.exchange((_, res) -> ReplayMapper.toDomain(res, Duration.ofNanos(System.nanoTime() - startNs)));
    }
}
