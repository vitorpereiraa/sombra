package com.github.vitorpereiraa.sombra.domain.json;

import java.util.stream.Collectors;

public final class JsonValueRenderer {

    private JsonValueRenderer() {}

    public static String render(JsonValue value) {
        return switch (value) {
            case JsonString s -> "\"" + escape(s.value()) + "\"";
            case JsonNumber n -> n.value().toPlainString();
            case JsonBoolean b -> Boolean.toString(b.value());
            case JsonNull ignored -> "null";
            case JsonArray a -> "[" + a.elements().stream()
                    .map(JsonValueRenderer::render)
                    .collect(Collectors.joining(",")) + "]";
            case JsonObject o -> "{" + o.fields().entrySet().stream()
                    .map(e -> "\"" + escape(e.getKey()) + "\":" + render(e.getValue()))
                    .collect(Collectors.joining(",")) + "}";
        };
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
