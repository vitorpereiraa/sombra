package com.github.vitorpereiraa.sombra.domain.json;

public sealed interface JsonValue permits JsonObject, JsonArray, JsonPrimitive, JsonNull {}
