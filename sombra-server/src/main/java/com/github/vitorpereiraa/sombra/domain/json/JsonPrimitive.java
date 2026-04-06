package com.github.vitorpereiraa.sombra.domain.json;

import static com.google.common.base.Preconditions.checkArgument;

public record JsonPrimitive(String value) implements JsonValue {

    public JsonPrimitive {
        checkArgument(value != null, "JsonPrimitive value cannot be null");
    }
}
