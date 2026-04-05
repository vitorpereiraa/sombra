package com.github.vitorpereiraa.sombra.domain;

import static com.google.common.base.Preconditions.checkArgument;

public record StatusCode(int value) {

    public StatusCode {
        checkArgument(value >= 100 && value <= 599,
            "StatusCode must be between 100 and 599, got: %s", value);
    }
}
