package com.github.vitorpereiraa.sombra.reporting;

import com.github.vitorpereiraa.sombra.domain.comparison.ResponseField;
import com.github.vitorpereiraa.sombra.domain.http.StatusCode;

final class ReportingTags {

    static final String STATUS_CLASS_NONE = "none";

    private ReportingTags() {}

    static String statusClass(StatusCode statusCode) {
        return (statusCode.value() / 100) + "xx";
    }

    static String fieldKind(ResponseField field) {
        return switch (field) {
            case ResponseField.StatusCode ignored -> "status";
            case ResponseField.Header ignored -> "header";
            case ResponseField.Body ignored -> "body";
        };
    }
}
