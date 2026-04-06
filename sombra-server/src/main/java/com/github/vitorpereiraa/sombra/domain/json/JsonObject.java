package com.github.vitorpereiraa.sombra.domain.json;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Map;

public record JsonObject(Map<String, JsonValue> fields) implements JsonValue {

    public JsonObject {
        checkArgument(fields != null, "JsonObject fields cannot be null");
        fields = Map.copyOf(fields);
    }
}
