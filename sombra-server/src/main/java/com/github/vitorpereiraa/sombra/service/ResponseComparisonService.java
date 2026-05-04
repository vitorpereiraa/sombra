package com.github.vitorpereiraa.sombra.service;

import com.github.vitorpereiraa.sombra.ComparisonProperties;
import com.github.vitorpereiraa.sombra.domain.comparison.ComparisonResult;
import com.github.vitorpereiraa.sombra.domain.comparison.Discrepancy;
import com.github.vitorpereiraa.sombra.domain.comparison.DiscrepancyValue;
import com.github.vitorpereiraa.sombra.domain.comparison.FieldPath;
import com.github.vitorpereiraa.sombra.domain.http.HttpHeader;
import com.github.vitorpereiraa.sombra.domain.http.HttpResponse;
import com.github.vitorpereiraa.sombra.domain.json.JsonComparator;
import com.github.vitorpereiraa.sombra.domain.json.JsonValue;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Component
public class ResponseComparisonService {

    private static final Logger log = LoggerFactory.getLogger(ResponseComparisonService.class);

    private final JsonMapper jsonMapper;
    private final JsonComparator jsonComparator;
    private final boolean compareHeaders;
    private final Set<String> ignoredHeaders;

    public ResponseComparisonService(ComparisonProperties properties, JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
        this.compareHeaders = properties.compareHeaders().orElse(false);
        this.ignoredHeaders = properties.ignoredHeaders().orElse(List.of()).stream()
                .map(String::toLowerCase)
                .collect(Collectors.toUnmodifiableSet());
        var ignoredFields = properties.ignoredFields().orElse(List.of()).stream()
                .map(FieldPath::new)
                .collect(Collectors.toUnmodifiableSet());
        this.jsonComparator = new JsonComparator(ignoredFields, properties.ignoreArrayOrder().orElse(false));
    }

    public ComparisonResult compare(HttpResponse original, HttpResponse candidate) {
        var discrepancies = new ArrayList<Discrepancy>();

        if (original.statusCode().value() != candidate.statusCode().value()) {
            discrepancies.add(Discrepancy.statusMismatch(
                    original.statusCode().value(), candidate.statusCode().value()));
        }

        if (compareHeaders) {
            discrepancies.addAll(compareHeaders(original, candidate));
        }

        discrepancies.addAll(compareBody(original, candidate));

        return new ComparisonResult(original, candidate, discrepancies);
    }

    List<Discrepancy> compareHeaders(HttpResponse original, HttpResponse candidate) {
        var discrepancies = new ArrayList<Discrepancy>();
        var origHeaders = toHeaderMap(original.headers());
        var candHeaders = toHeaderMap(candidate.headers());

        var allNames = new LinkedHashSet<>(origHeaders.keySet());
        allNames.addAll(candHeaders.keySet());

        for (var name : allNames) {
            if (ignoredHeaders.contains(name)) {
                continue;
            }

            var origValues = origHeaders.get(name);
            var candValues = candHeaders.get(name);

            if (origValues == null) {
                discrepancies.add(Discrepancy.headerAdded(name, candValues));
            } else if (candValues == null) {
                discrepancies.add(Discrepancy.headerRemoved(name, origValues));
            } else if (!origValues.equals(candValues)) {
                discrepancies.add(Discrepancy.headerMismatch(name, origValues, candValues));
            }
        }
        return discrepancies;
    }

    static Map<String, List<String>> toHeaderMap(List<HttpHeader> headers) {
        return headers.stream().collect(Collectors.toMap(
                h -> h.name().toLowerCase(),
                HttpHeader::values,
                (a, b) -> {
                    var merged = new ArrayList<>(a);
                    merged.addAll(b);
                    return List.copyOf(merged);
                }));
    }

    List<Discrepancy> compareBody(HttpResponse original, HttpResponse candidate) {
        var originalBody = original.body();
        var candidateBody = candidate.body();

        if (originalBody.isEmpty() && candidateBody.isEmpty()) {
            return List.of();
        }

        if (originalBody.isEmpty()) {
            return List.of(Discrepancy.bodyAdded(candidateBody.get().content()));
        }

        if (candidateBody.isEmpty()) {
            return List.of(Discrepancy.bodyRemoved(originalBody.get().content()));
        }

        var originalContent = originalBody.get().content();
        var candidateContent = candidateBody.get().content();

        var originalJson = parseJson(originalContent);
        var candidateJson = parseJson(candidateContent);

        if (originalJson.isPresent() && candidateJson.isPresent()) {
            return jsonComparator.compare(originalJson.get(), candidateJson.get(), new FieldPath("/"));
        }
        if (originalJson.isEmpty() && candidateJson.isEmpty()) {
            return originalContent.equals(candidateContent)
                    ? List.of()
                    : List.of(Discrepancy.bodyValueMismatch(originalContent, candidateContent));
        }
        return List.of(Discrepancy.bodyTypeMismatch(
                bodyValue(originalJson, originalContent),
                bodyValue(candidateJson, candidateContent)));
    }

    private static DiscrepancyValue bodyValue(Optional<JsonValue> json, String raw) {
        return json.<DiscrepancyValue>map(DiscrepancyValue.JsonBody::new)
                .orElseGet(() -> new DiscrepancyValue.RawBody(raw));
    }

    Optional<JsonValue> parseJson(String content) {
        try {
            return Optional.of(JsonValueMapper.toDomain(jsonMapper.readTree(content)));
        } catch (JacksonException e) {
            log.debug("Body is not valid JSON", e);
            return Optional.empty();
        }
    }
}
