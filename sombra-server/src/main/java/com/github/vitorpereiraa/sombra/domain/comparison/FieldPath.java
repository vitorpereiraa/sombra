package com.github.vitorpereiraa.sombra.domain.comparison;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Set;

public record FieldPath(String value) {

    public FieldPath {
        checkArgument(value != null, "FieldPath value cannot be null");
        checkArgument(!value.isBlank(), "FieldPath value cannot be blank");
        checkArgument(value.startsWith("/"), "FieldPath must start with '/', got: %s", value);
    }

    public FieldPath append(String segment) {
        return new FieldPath(value + "/" + segment);
    }

    public boolean isIgnoredBy(Set<FieldPath> ignored) {
        for (var field : ignored) {
            if (value.equals(field.value()) || value.startsWith(field.value() + "/")) {
                return true;
            }
        }
        return false;
    }
}
