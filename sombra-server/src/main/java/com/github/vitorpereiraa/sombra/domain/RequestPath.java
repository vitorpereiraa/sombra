package com.github.vitorpereiraa.sombra.domain;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public record RequestPath(String value) {

    public RequestPath {
        checkNotNull(value, "RequestPath value cannot be null");
        checkArgument(!value.isBlank(), "RequestPath value cannot be blank");
    }
}
