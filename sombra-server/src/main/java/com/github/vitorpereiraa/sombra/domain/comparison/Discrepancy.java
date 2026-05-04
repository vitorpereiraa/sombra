package com.github.vitorpereiraa.sombra.domain.comparison;

import static com.google.common.base.Preconditions.checkArgument;

import com.github.vitorpereiraa.sombra.domain.json.JsonValue;
import java.util.List;

public sealed interface Discrepancy {

    ResponseField field();

    static Discrepancy statusMismatch(int original, int candidate) {
        return new ValueMismatch(
                new ResponseField.StatusCode(),
                new DiscrepancyValue.Status(original),
                new DiscrepancyValue.Status(candidate));
    }

    static Discrepancy headerAdded(String name, List<String> candidate) {
        return new FieldAdded(new ResponseField.Header(name), new DiscrepancyValue.Headers(candidate));
    }

    static Discrepancy headerRemoved(String name, List<String> original) {
        return new FieldRemoved(new ResponseField.Header(name), new DiscrepancyValue.Headers(original));
    }

    static Discrepancy headerMismatch(String name, List<String> original, List<String> candidate) {
        return new ValueMismatch(
                new ResponseField.Header(name),
                new DiscrepancyValue.Headers(original),
                new DiscrepancyValue.Headers(candidate));
    }

    static Discrepancy bodyAdded(String content) {
        return new FieldAdded(new ResponseField.Body(), new DiscrepancyValue.RawBody(content));
    }

    static Discrepancy bodyRemoved(String content) {
        return new FieldRemoved(new ResponseField.Body(), new DiscrepancyValue.RawBody(content));
    }

    static Discrepancy bodyValueMismatch(String original, String candidate) {
        return new ValueMismatch(
                new ResponseField.Body(),
                new DiscrepancyValue.RawBody(original),
                new DiscrepancyValue.RawBody(candidate));
    }

    static Discrepancy bodyTypeMismatch(DiscrepancyValue original, DiscrepancyValue candidate) {
        return new TypeMismatch(new ResponseField.Body(), original, candidate);
    }

    static Discrepancy bodyAdded(FieldPath path, JsonValue candidate) {
        return new FieldAdded(new ResponseField.Body(path), new DiscrepancyValue.JsonBody(candidate));
    }

    static Discrepancy bodyRemoved(FieldPath path, JsonValue original) {
        return new FieldRemoved(new ResponseField.Body(path), new DiscrepancyValue.JsonBody(original));
    }

    static Discrepancy bodyValueMismatch(FieldPath path, JsonValue original, JsonValue candidate) {
        return new ValueMismatch(
                new ResponseField.Body(path),
                new DiscrepancyValue.JsonBody(original),
                new DiscrepancyValue.JsonBody(candidate));
    }

    static Discrepancy bodyTypeMismatch(FieldPath path, JsonValue original, JsonValue candidate) {
        return new TypeMismatch(
                new ResponseField.Body(path),
                new DiscrepancyValue.JsonBody(original),
                new DiscrepancyValue.JsonBody(candidate));
    }

    record ValueMismatch(ResponseField field, DiscrepancyValue originalValue, DiscrepancyValue candidateValue) implements Discrepancy {

        public ValueMismatch {
            checkArgument(field != null, "ValueMismatch field cannot be null");
            checkArgument(originalValue != null, "ValueMismatch originalValue cannot be null");
            checkArgument(candidateValue != null, "ValueMismatch candidateValue cannot be null");
        }
    }

    record FieldAdded(ResponseField field, DiscrepancyValue candidateValue) implements Discrepancy {

        public FieldAdded {
            checkArgument(field != null, "FieldAdded field cannot be null");
            checkArgument(candidateValue != null, "FieldAdded candidateValue cannot be null");
        }
    }

    record FieldRemoved(ResponseField field, DiscrepancyValue originalValue) implements Discrepancy {

        public FieldRemoved {
            checkArgument(field != null, "FieldRemoved field cannot be null");
            checkArgument(originalValue != null, "FieldRemoved originalValue cannot be null");
        }
    }

    record TypeMismatch(ResponseField field, DiscrepancyValue originalValue, DiscrepancyValue candidateValue) implements Discrepancy {

        public TypeMismatch {
            checkArgument(field != null, "TypeMismatch field cannot be null");
            checkArgument(originalValue != null, "TypeMismatch originalValue cannot be null");
            checkArgument(candidateValue != null, "TypeMismatch candidateValue cannot be null");
        }
    }
}
