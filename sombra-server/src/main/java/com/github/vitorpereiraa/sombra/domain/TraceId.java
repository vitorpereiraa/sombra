package com.github.vitorpereiraa.sombra.domain;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public record TraceId(String value) {

    public TraceId {
        checkNotNull(value, "TraceId value cannot be null");
        checkArgument(!value.isBlank(), "TraceId value cannot be blank");
    }
}
