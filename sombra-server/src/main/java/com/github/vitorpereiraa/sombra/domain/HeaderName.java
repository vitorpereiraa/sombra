package com.github.vitorpereiraa.sombra.domain;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public record HeaderName(String value) {

    public HeaderName {
        checkNotNull(value, "HeaderName value cannot be null");
        checkArgument(!value.isBlank(), "HeaderName value cannot be blank");
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof HeaderName other && value.equalsIgnoreCase(other.value);
    }

    @Override
    public int hashCode() {
        return value.toLowerCase().hashCode();
    }
}
