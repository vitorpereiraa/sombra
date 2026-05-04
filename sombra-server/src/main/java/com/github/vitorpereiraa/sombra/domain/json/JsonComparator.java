package com.github.vitorpereiraa.sombra.domain.json;

import com.github.vitorpereiraa.sombra.domain.comparison.Discrepancy;
import com.github.vitorpereiraa.sombra.domain.comparison.FieldPath;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class JsonComparator {

    private final Set<FieldPath> ignoredFields;
    private final boolean ignoreArrayOrder;

    public JsonComparator(Set<FieldPath> ignoredFields, boolean ignoreArrayOrder) {
        this.ignoredFields = ignoredFields;
        this.ignoreArrayOrder = ignoreArrayOrder;
    }

    public List<Discrepancy> compare(JsonValue original, JsonValue candidate, FieldPath path) {
        if (path.isIgnoredBy(ignoredFields)) {
            return List.of();
        }

        return switch (original) {
            case JsonObject orig when candidate instanceof JsonObject cand -> compareObjects(orig, cand, path);
            case JsonArray orig when candidate instanceof JsonArray cand -> compareArrays(orig, cand, path);
            case JsonPrimitive orig when candidate instanceof JsonPrimitive cand -> comparePrimitives(orig, cand, path);
            default -> List.of(Discrepancy.bodyTypeMismatch(path, original, candidate));
        };
    }

    static List<Discrepancy> comparePrimitives(JsonPrimitive original, JsonPrimitive candidate, FieldPath path) {
        return switch (original) {
            case JsonString orig when candidate instanceof JsonString cand ->
                orig.value().equals(cand.value()) ? List.of() : List.of(Discrepancy.bodyValueMismatch(path, orig, cand));
            case JsonNumber orig when candidate instanceof JsonNumber cand ->
                orig.value().compareTo(cand.value()) == 0 ? List.of() : List.of(Discrepancy.bodyValueMismatch(path, orig, cand));
            case JsonBoolean orig when candidate instanceof JsonBoolean cand ->
                orig.value() == cand.value() ? List.of() : List.of(Discrepancy.bodyValueMismatch(path, orig, cand));
            case JsonNull _ when candidate instanceof JsonNull _ -> List.of();
            default -> List.of(Discrepancy.bodyTypeMismatch(path, original, candidate));
        };
    }

    List<Discrepancy> compareObjects(JsonObject original, JsonObject candidate, FieldPath path) {
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
                discrepancies.add(Discrepancy.bodyAdded(fieldPath, candValue));
            } else if (candValue == null) {
                discrepancies.add(Discrepancy.bodyRemoved(fieldPath, origValue));
            } else {
                discrepancies.addAll(compare(origValue, candValue, fieldPath));
            }
        }
        return discrepancies;
    }

    List<Discrepancy> compareArrays(JsonArray original, JsonArray candidate, FieldPath path) {
        if (ignoreArrayOrder) {
            return compareArraysUnordered(original, candidate, path);
        }

        var origElements = original.elements();
        var candElements = candidate.elements();
        var discrepancies = new ArrayList<Discrepancy>();
        int maxSize = Math.max(origElements.size(), candElements.size());

        for (int i = 0; i < maxSize; i++) {
            var elementPath = path.append(String.valueOf(i));
            if (elementPath.isIgnoredBy(ignoredFields)) {
                continue;
            }

            if (i >= origElements.size()) {
                discrepancies.add(Discrepancy.bodyAdded(elementPath, candElements.get(i)));
            } else if (i >= candElements.size()) {
                discrepancies.add(Discrepancy.bodyRemoved(elementPath, origElements.get(i)));
            } else {
                discrepancies.addAll(compare(origElements.get(i), candElements.get(i), elementPath));
            }
        }
        return discrepancies;
    }

    static List<Discrepancy> compareArraysUnordered(JsonArray original, JsonArray candidate, FieldPath path) {
        var remaining = new ArrayList<>(candidate.elements());
        var discrepancies = new ArrayList<Discrepancy>();
        for (var orig : original.elements()) {
            if (!remaining.remove(orig)) {
                discrepancies.add(Discrepancy.bodyRemoved(path, orig));
            }
        }
        for (var added : remaining) {
            discrepancies.add(Discrepancy.bodyAdded(path, added));
        }
        return discrepancies;
    }
}
