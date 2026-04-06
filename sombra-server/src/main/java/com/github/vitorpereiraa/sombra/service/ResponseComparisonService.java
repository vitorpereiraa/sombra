package com.github.vitorpereiraa.sombra.service;

import com.github.vitorpereiraa.sombra.ComparisonProperties;
import com.github.vitorpereiraa.sombra.domain.ComparisonResult;
import com.github.vitorpereiraa.sombra.domain.Discrepancy;
import com.github.vitorpereiraa.sombra.domain.FieldPath;
import com.github.vitorpereiraa.sombra.domain.HttpResponse;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Component
public class ResponseComparisonService {

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

        compareBody(original, candidate, discrepancies);

        var result = new ComparisonResult(discrepancies);
        lastResult = result;
        return result;
    }

    public ComparisonResult lastResult() {
        return lastResult;
    }

    void compareBody(HttpResponse original, HttpResponse candidate, ArrayList<Discrepancy> discrepancies) {
        var originalBody = original.body();
        var candidateBody = candidate.body();

        if (originalBody.isEmpty() && candidateBody.isEmpty()) {
            return;
        }

        if (originalBody.isEmpty()) {
            discrepancies.add(new Discrepancy(new FieldPath("/body"), "<absent>", candidateBody.get().content()));
            return;
        }

        if (candidateBody.isEmpty()) {
            discrepancies.add(new Discrepancy(new FieldPath("/body"), originalBody.get().content(), "<absent>"));
            return;
        }

        var originalContent = originalBody.get().content();
        var candidateContent = candidateBody.get().content();

        try {
            var originalJson = JsonValueMapper.toDomain(jsonMapper.readTree(originalContent));
            var candidateJson = JsonValueMapper.toDomain(jsonMapper.readTree(candidateContent));
            discrepancies.addAll(BodyComparator.compare(originalJson, candidateJson, new FieldPath("/body"), ignoredFields));
        } catch (JacksonException e) {
            if (!originalContent.equals(candidateContent)) {
                discrepancies.add(new Discrepancy(new FieldPath("/body"), originalContent, candidateContent));
            }
        }
    }
}
