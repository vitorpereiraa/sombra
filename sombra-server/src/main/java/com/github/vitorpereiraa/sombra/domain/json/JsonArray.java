package com.github.vitorpereiraa.sombra.domain.json;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;

public record JsonArray(List<JsonValue> elements) implements JsonValue {

    public JsonArray {
        checkArgument(elements != null, "JsonArray elements cannot be null");
        elements = List.copyOf(elements);
    }
}
