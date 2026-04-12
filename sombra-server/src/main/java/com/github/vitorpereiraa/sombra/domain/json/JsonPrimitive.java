package com.github.vitorpereiraa.sombra.domain.json;

public sealed interface JsonPrimitive extends JsonValue permits JsonString, JsonNumber, JsonBoolean, JsonNull {}
