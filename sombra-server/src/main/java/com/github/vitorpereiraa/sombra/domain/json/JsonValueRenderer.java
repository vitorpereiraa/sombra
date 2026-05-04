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
        var sb = new StringBuilder(s.length() + 2);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"' -> sb.append("\\\"");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}
