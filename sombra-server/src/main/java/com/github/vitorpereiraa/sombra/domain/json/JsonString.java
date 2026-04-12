package com.github.vitorpereiraa.sombra.domain.json;

import static com.google.common.base.Preconditions.checkArgument;

public record JsonString(String value) implements JsonPrimitive {

    public JsonString {
        checkArgument(value != null, "JsonString value cannot be null");
    }
}
