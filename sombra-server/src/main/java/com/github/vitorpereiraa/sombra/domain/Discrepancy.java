package com.github.vitorpereiraa.sombra.domain;

import static com.google.common.base.Preconditions.checkArgument;

public record Discrepancy(FieldPath path, String expected, String actual) {

    public Discrepancy {
        checkArgument(path != null, "Discrepancy path cannot be null");
        checkArgument(expected != null, "Discrepancy expected cannot be null");
        checkArgument(actual != null, "Discrepancy actual cannot be null");
    }
}
