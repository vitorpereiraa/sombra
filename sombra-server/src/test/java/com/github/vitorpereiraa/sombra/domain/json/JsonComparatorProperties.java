package com.github.vitorpereiraa.sombra.domain.json;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.vitorpereiraa.sombra.domain.comparison.Discrepancy;
import com.github.vitorpereiraa.sombra.domain.comparison.FieldPath;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.IntRange;

class JsonComparatorProperties {

    private static final FieldPath ROOT = new FieldPath("/");

    private static JsonComparator comparator(boolean ignoreArrayOrder) {
        return new JsonComparator(Set.of(), ignoreArrayOrder);
    }

    @Provide
    Arbitrary<JsonValue> json() {
        return JsonArbitraries.jsonValues();
    }

    @Provide
    Arbitrary<List<JsonValue>> jsonLists() {
        return JsonArbitraries.jsonValues().list().ofMaxSize(6);
    }

    @Provide
    Arbitrary<String> segments() {
        return Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(5);
    }

    // Reflexivity: comparing any value with itself yields no discrepancies, in either array mode.
    @Property
    void identicalValuesProduceNoDiscrepancies(@ForAll("json") JsonValue value) {
        assertThat(comparator(false).compare(value, value, ROOT)).isEmpty();
        assertThat(comparator(true).compare(value, value, ROOT)).isEmpty();
    }

    // Numbers are compared by value (compareTo == 0), so differences in scale never matter.
    @Property
    void numbersDifferingOnlyInScaleMatch(@ForAll @IntRange(min = -100000, max = 100000) int n) {
        var unscaled = new JsonNumber(BigDecimal.valueOf(n));
        var scaled = new JsonNumber(BigDecimal.valueOf(n).setScale(3));
        assertThat(comparator(false).compare(unscaled, scaled, ROOT)).isEmpty();
    }

    // Swapping original and candidate turns every FieldAdded into a FieldRemoved and vice versa,
    // while value- and type-mismatch counts are preserved.
    @Property
    void swappingOperandsMirrorsAddedAndRemoved(@ForAll("json") JsonValue a, @ForAll("json") JsonValue b) {
        var forward = comparator(false).compare(a, b, ROOT);
        var backward = comparator(false).compare(b, a, ROOT);

        assertThat(backward).hasSameSizeAs(forward);
        assertThat(count(backward, Discrepancy.FieldAdded.class)).isEqualTo(count(forward, Discrepancy.FieldRemoved.class));
        assertThat(count(backward, Discrepancy.FieldRemoved.class)).isEqualTo(count(forward, Discrepancy.FieldAdded.class));
        assertThat(count(backward, Discrepancy.ValueMismatch.class)).isEqualTo(count(forward, Discrepancy.ValueMismatch.class));
        assertThat(count(backward, Discrepancy.TypeMismatch.class)).isEqualTo(count(forward, Discrepancy.TypeMismatch.class));
    }

    // Adding a field to the ignore set can only reduce (never increase) the discrepancy count.
    @Property
    void ignoringAFieldNeverIncreasesDiscrepancies(
            @ForAll("json") JsonValue a, @ForAll("json") JsonValue b, @ForAll("segments") String segment) {
        var withoutIgnore = new JsonComparator(Set.of(), false).compare(a, b, ROOT);
        var withIgnore = new JsonComparator(Set.of(new FieldPath("/" + segment)), false).compare(a, b, ROOT);
        assertThat(withIgnore.size()).isLessThanOrEqualTo(withoutIgnore.size());
    }

    // Ignoring the root path short-circuits the whole comparison to a match.
    @Property
    void ignoringRootProducesNoDiscrepancies(@ForAll("json") JsonValue a, @ForAll("json") JsonValue b) {
        var comparator = new JsonComparator(Set.of(ROOT), false);
        assertThat(comparator.compare(a, b, ROOT)).isEmpty();
    }

    // With ignoreArrayOrder enabled, any permutation of an array compares clean (multiset equality).
    @Property
    void permutedArraysMatchWhenOrderIsIgnored(@ForAll("jsonLists") List<JsonValue> elements, @ForAll long seed) {
        var shuffled = new ArrayList<>(elements);
        Collections.shuffle(shuffled, new Random(seed));
        var original = new JsonArray(elements);
        var candidate = new JsonArray(shuffled);
        assertThat(comparator(true).compare(original, candidate, ROOT)).isEmpty();
    }

    // Differing JSON kinds at a path produce exactly one TypeMismatch.
    @Property
    void stringVersusNumberIsSingleTypeMismatch(@ForAll String text, @ForAll int number) {
        var result = comparator(false)
                .compare(new JsonString(text), new JsonNumber(BigDecimal.valueOf(number)), ROOT);
        assertThat(result).singleElement().isInstanceOf(Discrepancy.TypeMismatch.class);
    }

    private static long count(List<Discrepancy> discrepancies, Class<? extends Discrepancy> type) {
        return discrepancies.stream().filter(type::isInstance).count();
    }
}
