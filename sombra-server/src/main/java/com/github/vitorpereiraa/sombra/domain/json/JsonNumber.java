package com.github.vitorpereiraa.sombra.domain.json;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigDecimal;

public record JsonNumber(BigDecimal value) implements JsonPrimitive {

    public JsonNumber {
        checkArgument(value != null, "JsonNumber value cannot be null");
    }
}
