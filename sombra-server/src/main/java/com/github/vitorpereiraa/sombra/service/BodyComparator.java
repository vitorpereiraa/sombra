package com.github.vitorpereiraa.sombra.service;

import com.github.vitorpereiraa.sombra.domain.Discrepancy;
import com.github.vitorpereiraa.sombra.domain.FieldPath;
import com.github.vitorpereiraa.sombra.domain.json.JsonArray;
import com.github.vitorpereiraa.sombra.domain.json.JsonNull;
import com.github.vitorpereiraa.sombra.domain.json.JsonObject;
import com.github.vitorpereiraa.sombra.domain.json.JsonPrimitive;
import com.github.vitorpereiraa.sombra.domain.json.JsonValue;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

class BodyComparator {

    static List<Discrepancy> compare(JsonValue original, JsonValue candidate, FieldPath path, Set<FieldPath> ignoredFields) {
        if (path.isIgnoredBy(ignoredFields)) {
            return List.of();
        }

        return switch (original) {
            case JsonObject orig when candidate instanceof JsonObject cand -> compareObjects(orig, cand, path, ignoredFields);
            case JsonArray orig when candidate instanceof JsonArray cand -> compareArrays(orig, cand, path, ignoredFields);
            case JsonPrimitive orig when candidate instanceof JsonPrimitive cand -> comparePrimitives(orig, cand, path);
            case JsonNull _ when candidate instanceof JsonNull _ -> List.of();
            default -> List.of(new Discrepancy(path, stringify(original), stringify(candidate)));
        };
    }

    static List<Discrepancy> compareObjects(
            JsonObject original, JsonObject candidate, FieldPath path, Set<FieldPath> ignoredFields) {
        var discrepancies = new ArrayList<Discrepancy>();
        var allKeys = new LinkedHashSet<>(original.fields().keySet());
        allKeys.addAll(candidate.fields().keySet());

        for (var key : allKeys) {
            var fieldPath = path.append(key);
            if (fieldPath.isIgnoredBy(ignoredFields)) {
                continue;
            }

            var origValue = original.fields().get(key);
            var candValue = candidate.fields().get(key);

            if (origValue == null) {
                discrepancies.add(new Discrepancy(fieldPath, "<absent>", stringify(candValue)));
            } else if (candValue == null) {
                discrepancies.add(new Discrepancy(fieldPath, stringify(origValue), "<absent>"));
            } else {
                discrepancies.addAll(compare(origValue, candValue, fieldPath, ignoredFields));
            }
        }
        return discrepancies;
    }

    static List<Discrepancy> compareArrays(
            JsonArray original, JsonArray candidate, FieldPath path, Set<FieldPath> ignoredFields) {
        var discrepancies = new ArrayList<Discrepancy>();
        int maxSize = Math.max(original.elements().size(), candidate.elements().size());

        for (int i = 0; i < maxSize; i++) {
            var elementPath = path.append(String.valueOf(i));
            if (elementPath.isIgnoredBy(ignoredFields)) {
                continue;
            }

            if (i >= original.elements().size()) {
                discrepancies.add(new Discrepancy(elementPath, "<absent>", stringify(candidate.elements().get(i))));
            } else if (i >= candidate.elements().size()) {
                discrepancies.add(new Discrepancy(elementPath, stringify(original.elements().get(i)), "<absent>"));
            } else {
                discrepancies.addAll(compare(original.elements().get(i), candidate.elements().get(i), elementPath, ignoredFields));
            }
        }
        return discrepancies;
    }

    static List<Discrepancy> comparePrimitives(JsonPrimitive original, JsonPrimitive candidate, FieldPath path) {
        if (original.value().equals(candidate.value())) {
            return List.of();
        }
        return List.of(new Discrepancy(path, original.value(), candidate.value()));
    }

    static String stringify(JsonValue value) {
        return switch (value) {
            case JsonPrimitive p -> p.value();
            case JsonNull _ -> "null";
            case JsonObject _ -> "<object>";
            case JsonArray _ -> "<array>";
        };
    }
}
