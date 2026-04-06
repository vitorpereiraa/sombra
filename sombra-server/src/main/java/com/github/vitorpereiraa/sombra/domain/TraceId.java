package com.github.vitorpereiraa.sombra.domain;

import static com.google.common.base.Preconditions.checkArgument;

public record TraceId(String value) {

    public TraceId {
        checkArgument(value != null, "TraceId value cannot be null");
        checkArgument(!value.isBlank(), "TraceId value cannot be blank");
    }
}
