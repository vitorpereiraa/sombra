package com.github.vitorpereiraa.sombra.domain.json;

import java.math.BigDecimal;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

/**
 * Shared jqwik generators for the {@link JsonValue} algebra. Reused across the comparator,
 * round-trip, and service property tests so JSON generation lives in one place.
 *
 * <p>Recursion is biased toward leaves (primitives weighted higher than containers) so generated
 * trees terminate quickly and shrink fast. String content deliberately includes characters that
 * require JSON escaping ({@code " \ \n \t}) to exercise the renderer.
 */
public final class JsonArbitraries {

    private JsonArbitraries() {}

    private static final int MAX_DEPTH = 3;
    private static final int MAX_CONTAINER_SIZE = 3;

    public static Arbitrary<JsonValue> jsonValues() {
        return jsonValues(MAX_DEPTH);
    }

    // Depth-bounded recursion keeps generated trees small (and shrinking fast); primitives are
    // weighted at every level so most generated values stay shallow.
    private static Arbitrary<JsonValue> jsonValues(int depth) {
        if (depth <= 0) {
            return jsonPrimitives();
        }
        var element = jsonValues(depth - 1);
        var arrays = element.list().ofMaxSize(MAX_CONTAINER_SIZE).map(JsonArray::new);
        var keys = Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(5);
        var objects = Arbitraries.maps(keys, element).ofMaxSize(MAX_CONTAINER_SIZE).map(JsonObject::new);
        return Arbitraries.oneOf(jsonPrimitives(), jsonPrimitives(), arrays, objects);
    }

    public static Arbitrary<JsonValue> jsonPrimitives() {
        return Arbitraries.oneOf(jsonStrings(), jsonNumbers(), jsonBooleans(), jsonNulls());
    }

    static Arbitrary<JsonValue> jsonStrings() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withChars("0123456789 _-.\"\\\n\t")
                .ofMaxLength(8)
                .map(JsonString::new);
    }

    static Arbitrary<JsonValue> jsonNumbers() {
        var integral = Arbitraries.integers().between(-100000, 100000).map(i -> BigDecimal.valueOf((long) i));
        var scaled = Arbitraries.integers().between(-100000, 100000).map(i -> BigDecimal.valueOf(i, 2));
        return Arbitraries.oneOf(integral, scaled).map(JsonNumber::new);
    }

    static Arbitrary<JsonValue> jsonBooleans() {
        return Arbitraries.of(true, false).map(JsonBoolean::new);
    }

    static Arbitrary<JsonValue> jsonNulls() {
        return Arbitraries.just((JsonValue) new JsonNull());
    }
}
