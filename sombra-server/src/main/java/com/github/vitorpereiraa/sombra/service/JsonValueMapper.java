package com.github.vitorpereiraa.sombra.service;

import com.github.vitorpereiraa.sombra.domain.json.JsonArray;
import com.github.vitorpereiraa.sombra.domain.json.JsonBoolean;
import com.github.vitorpereiraa.sombra.domain.json.JsonNull;
import com.github.vitorpereiraa.sombra.domain.json.JsonNumber;
import com.github.vitorpereiraa.sombra.domain.json.JsonObject;
import com.github.vitorpereiraa.sombra.domain.json.JsonString;
import com.github.vitorpereiraa.sombra.domain.json.JsonValue;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import tools.jackson.databind.JsonNode;

class JsonValueMapper {

    static JsonValue toDomain(JsonNode node) {
        if (node == null || node.isNull()) {
            return new JsonNull();
        }
        if (node.isObject()) {
            var fields = new LinkedHashMap<String, JsonValue>();
            for (var entry : node.properties()) {
                fields.put(entry.getKey(), toDomain(entry.getValue()));
            }
            return new JsonObject(fields);
        }
        if (node.isArray()) {
            var elements = new ArrayList<JsonValue>();
            for (var element : node) {
                elements.add(toDomain(element));
            }
            return new JsonArray(elements);
        }
        if (node.isTextual()) {
            return new JsonString(node.textValue());
        }
        if (node.isNumber()) {
            return new JsonNumber(node.decimalValue());
        }
        if (node.isBoolean()) {
            return new JsonBoolean(node.booleanValue());
        }
        throw new IllegalArgumentException("Unsupported JSON node kind: " + node.getNodeType());
    }
}
