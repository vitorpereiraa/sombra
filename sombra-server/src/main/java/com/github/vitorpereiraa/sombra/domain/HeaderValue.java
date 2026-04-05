package com.github.vitorpereiraa.sombra.domain;

import static com.google.common.base.Preconditions.checkNotNull;

public record HeaderValue(String value) {

    public HeaderValue {
        checkNotNull(value, "HeaderValue value cannot be null");
    }
}
