package com.github.vitorpereiraa.sombra.service;

import com.github.vitorpereiraa.sombra.ComparisonProperties;
import com.github.vitorpereiraa.sombra.domain.ComparisonResult;
import com.github.vitorpereiraa.sombra.domain.Discrepancy;
import com.github.vitorpereiraa.sombra.domain.FieldPath;
import com.github.vitorpereiraa.sombra.domain.HttpResponse;
import java.util.ArrayList;
import java.util.List;
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
    private final Set<FieldPath> ignoredFields;
    private volatile ComparisonResult lastResult;

    public ResponseComparisonService(ComparisonProperties properties, JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
        this.ignoredFields = properties.ignoredFields().stream()
                .map(FieldPath::new)
                .collect(Collectors.toUnmodifiableSet());
    }

    public ComparisonResult compare(HttpResponse original, HttpResponse candidate) {
        var discrepancies = new ArrayList<Discrepancy>();

        if (original.statusCode().value() != candidate.statusCode().value()) {
            discrepancies.add(new Discrepancy(
                    new FieldPath("/statusCode"),
                    String.valueOf(original.statusCode().value()),
                    String.valueOf(candidate.statusCode().value())));
        }

        discrepancies.addAll(compareBody(original, candidate));

        var result = new ComparisonResult(discrepancies);
        lastResult = result;
        return result;
    }

    public ComparisonResult lastResult() {
        return lastResult;
    }

    List<Discrepancy> compareBody(HttpResponse original, HttpResponse candidate) {
        var originalBody = original.body();
        var candidateBody = candidate.body();

        if (originalBody.isEmpty() && candidateBody.isEmpty()) {
            return List.of();
        }

        if (originalBody.isEmpty()) {
            return List.of(new Discrepancy(new FieldPath("/body"), "<absent>", candidateBody.get().content()));
        }

        if (candidateBody.isEmpty()) {
            return List.of(new Discrepancy(new FieldPath("/body"), originalBody.get().content(), "<absent>"));
        }

        var originalContent = originalBody.get().content();
        var candidateContent = candidateBody.get().content();

        try {
            var originalJson = JsonValueMapper.toDomain(jsonMapper.readTree(originalContent));
            var candidateJson = JsonValueMapper.toDomain(jsonMapper.readTree(candidateContent));
            return BodyComparator.compare(originalJson, candidateJson, new FieldPath("/body"), ignoredFields);
        } catch (JacksonException e) {
            log.debug("Bodies are not valid JSON, falling back to string comparison", e);
            if (!originalContent.equals(candidateContent)) {
                return List.of(new Discrepancy(new FieldPath("/body"), originalContent, candidateContent));
            }
            return List.of();
        }
    }
}
