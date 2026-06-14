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

    private record Both(JsonValue original, JsonValue candidate) {}

    public List<Discrepancy> compare(JsonValue original, JsonValue candidate, FieldPath path) {
        return compare(new Both(original, candidate), path);
    }

    private List<Discrepancy> compare(Both both, FieldPath path) {
        if (path.isIgnoredBy(ignoredFields)) {
            return List.of();
        }

        return switch (both) {
            case Both(JsonObject o, JsonObject c)   -> compareObjects(o, c, path);
            case Both(JsonArray o, JsonArray c)     -> compareArrays(o, c, path);
            case Both(JsonString o, JsonString c)   -> diff(!o.value().equals(c.value()), path, o, c);
            case Both(JsonNumber o, JsonNumber c)   -> diff(o.value().compareTo(c.value()) != 0, path, o, c);
            case Both(JsonBoolean o, JsonBoolean c) -> diff(o.value() != c.value(), path, o, c);
            case Both(JsonNull _, JsonNull _)       -> List.of();
            default -> List.of(Discrepancy.bodyTypeMismatch(path, both.original(), both.candidate()));
        };
    }

    private static List<Discrepancy> diff(boolean differ, FieldPath path, JsonValue o, JsonValue c) {
        return differ ? List.of(Discrepancy.bodyValueMismatch(path, o, c)) : List.of();
    }

    private List<Discrepancy> compareChild(JsonValue orig, JsonValue cand, FieldPath childPath) {
        if (childPath.isIgnoredBy(ignoredFields)) {
            return List.of();
        }
        if (orig == null) {
            return List.of(Discrepancy.bodyAdded(childPath, cand));
        }
        if (cand == null) {
            return List.of(Discrepancy.bodyRemoved(childPath, orig));
        }
        return compare(new Both(orig, cand), childPath);
    }

    List<Discrepancy> compareObjects(JsonObject original, JsonObject candidate, FieldPath path) {
        var allKeys = new LinkedHashSet<>(original.fields().keySet());
        allKeys.addAll(candidate.fields().keySet());

        var discrepancies = new ArrayList<Discrepancy>();
        for (var key : allKeys) {
            discrepancies.addAll(
                    compareChild(original.fields().get(key), candidate.fields().get(key), path.append(key)));
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
            var orig = i < origElements.size() ? origElements.get(i) : null;
            var cand = i < candElements.size() ? candElements.get(i) : null;
            discrepancies.addAll(compareChild(orig, cand, path.append(String.valueOf(i))));
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
