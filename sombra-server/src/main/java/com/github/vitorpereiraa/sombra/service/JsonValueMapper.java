package com.github.vitorpereiraa.sombra.service;

import com.github.vitorpereiraa.sombra.domain.json.JsonArray;
import com.github.vitorpereiraa.sombra.domain.json.JsonNull;
import com.github.vitorpereiraa.sombra.domain.json.JsonObject;
import com.github.vitorpereiraa.sombra.domain.json.JsonPrimitive;
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
        return new JsonPrimitive(node.toString());
    }
}
