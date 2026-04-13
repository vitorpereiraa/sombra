package com.github.vitorpereiraa.sombra.reporting;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.vitorpereiraa.sombra.domain.comparison.Discrepancy;
import com.github.vitorpereiraa.sombra.domain.comparison.FieldPath;
import com.github.vitorpereiraa.sombra.domain.comparison.ResponseField;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
record ReportedDiscrepancy(
        String type, String fieldKind, Optional<String> name, Optional<String> path) {

    static ReportedDiscrepancy from(Discrepancy discrepancy) {
        var type = discrepancy.getClass().getSimpleName();
        return switch (discrepancy.field()) {
            case ResponseField.StatusCode ignored ->
                    new ReportedDiscrepancy(type, "status", Optional.empty(), Optional.empty());
            case ResponseField.Header header ->
                    new ReportedDiscrepancy(type, "header", Optional.of(header.name()), Optional.empty());
            case ResponseField.Body body ->
                    new ReportedDiscrepancy(type, "body", Optional.empty(), body.path().map(FieldPath::value));
        };
    }
}
