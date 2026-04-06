package com.github.vitorpereiraa.sombra.domain;

import static com.google.common.base.Preconditions.checkArgument;

public record RequestPath(String value) {

    public RequestPath {
        checkArgument(value != null, "RequestPath value cannot be null");
        checkArgument(!value.isBlank(), "RequestPath value cannot be blank");
    }
}
