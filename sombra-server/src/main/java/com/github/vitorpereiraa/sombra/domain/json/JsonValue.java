package com.github.vitorpereiraa.sombra.domain.json;

public sealed interface JsonValue permits JsonObject, JsonArray, JsonPrimitive, JsonNull {

    default String displayValue() {
        return switch (this) {
            case JsonPrimitive p -> p.value();
            case JsonNull _ -> "null";
            case JsonObject _ -> "<object>";
            case JsonArray _ -> "<array>";
        };
    }
}
