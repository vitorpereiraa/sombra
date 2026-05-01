package com.github.vitorpereiraa.sombra.domain.reporting;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.vitorpereiraa.sombra.domain.comparison.Discrepancy;
import com.github.vitorpereiraa.sombra.domain.comparison.FieldPath;
import com.github.vitorpereiraa.sombra.domain.comparison.ResponseField;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record ReportedDiscrepancy(
        String type,
        String fieldKind,
        Optional<String> name,
        Optional<String> path,
        Optional<String> originalValue,
        Optional<String> candidateValue) {

    public static ReportedDiscrepancy from(Discrepancy discrepancy) {
        var type = discrepancy.getClass().getSimpleName();
        var values = extractValues(discrepancy);
        return switch (discrepancy.field()) {
            case ResponseField.StatusCode ignored ->
                    new ReportedDiscrepancy(type, "status", Optional.empty(), Optional.empty(),
                            values.original(), values.candidate());
            case ResponseField.Header header ->
                    new ReportedDiscrepancy(type, "header", Optional.of(header.name()), Optional.empty(),
                            values.original(), values.candidate());
            case ResponseField.Body body ->
                    new ReportedDiscrepancy(type, "body", Optional.empty(), body.path().map(FieldPath::value),
                            values.original(), values.candidate());
        };
    }

    private static Values extractValues(Discrepancy discrepancy) {
        return switch (discrepancy) {
            case Discrepancy.ValueMismatch v ->
                    new Values(Optional.of(v.originalValue()), Optional.of(v.candidateValue()));
            case Discrepancy.TypeMismatch t ->
                    new Values(Optional.of(t.originalValue()), Optional.of(t.candidateValue()));
            case Discrepancy.FieldAdded a ->
                    new Values(Optional.empty(), Optional.of(a.candidateValue()));
            case Discrepancy.FieldRemoved r ->
                    new Values(Optional.of(r.originalValue()), Optional.empty());
        };
    }

    private record Values(Optional<String> original, Optional<String> candidate) {}
}
