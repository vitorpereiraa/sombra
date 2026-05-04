package com.github.vitorpereiraa.sombra.domain.reporting;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.vitorpereiraa.sombra.domain.comparison.Discrepancy;
import com.github.vitorpereiraa.sombra.domain.comparison.DiscrepancyValue;
import com.github.vitorpereiraa.sombra.domain.comparison.FieldPath;
import com.github.vitorpereiraa.sombra.domain.comparison.ResponseField;
import com.github.vitorpereiraa.sombra.domain.json.JsonValueRenderer;
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
        var values = extractValues(discrepancy);
        return switch (discrepancy.field()) {
            case ResponseField.StatusCode ignored ->
                    new ReportedDiscrepancy(typeOf(discrepancy), "status", Optional.empty(), Optional.empty(),
                            values.original(), values.candidate());
            case ResponseField.Header header ->
                    new ReportedDiscrepancy(typeOf(discrepancy), "header", Optional.of(header.name()), Optional.empty(),
                            values.original(), values.candidate());
            case ResponseField.Body body ->
                    new ReportedDiscrepancy(typeOf(discrepancy), "body", Optional.empty(), body.path().map(FieldPath::value),
                            values.original(), values.candidate());
        };
    }

    public static String typeOf(Discrepancy discrepancy) {
        return discrepancy.getClass().getSimpleName();
    }

    public static String fieldKindOf(ResponseField field) {
        return switch (field) {
            case ResponseField.StatusCode ignored -> "status";
            case ResponseField.Header ignored -> "header";
            case ResponseField.Body ignored -> "body";
        };
    }

    private static Values extractValues(Discrepancy discrepancy) {
        return switch (discrepancy) {
            case Discrepancy.ValueMismatch v ->
                    new Values(Optional.of(render(v.originalValue())), Optional.of(render(v.candidateValue())));
            case Discrepancy.TypeMismatch t ->
                    new Values(Optional.of(render(t.originalValue())), Optional.of(render(t.candidateValue())));
            case Discrepancy.FieldAdded a ->
                    new Values(Optional.empty(), Optional.of(render(a.candidateValue())));
            case Discrepancy.FieldRemoved r ->
                    new Values(Optional.of(render(r.originalValue())), Optional.empty());
        };
    }

    private static String render(DiscrepancyValue value) {
        return switch (value) {
            case DiscrepancyValue.Status s -> String.valueOf(s.code());
            case DiscrepancyValue.Headers h -> String.join(", ", h.values());
            case DiscrepancyValue.JsonBody b -> JsonValueRenderer.render(b.value());
            case DiscrepancyValue.RawBody r -> r.content();
        };
    }

    private record Values(Optional<String> original, Optional<String> candidate) {}
}
