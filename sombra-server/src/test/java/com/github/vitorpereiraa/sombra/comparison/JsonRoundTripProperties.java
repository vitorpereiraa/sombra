package com.github.vitorpereiraa.sombra.comparison;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.vitorpereiraa.sombra.domain.comparison.FieldPath;
import com.github.vitorpereiraa.sombra.domain.json.JsonArbitraries;
import com.github.vitorpereiraa.sombra.domain.json.JsonComparator;
import com.github.vitorpereiraa.sombra.domain.json.JsonValue;
import com.github.vitorpereiraa.sombra.domain.json.JsonValueRenderer;
import java.util.Set;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import tools.jackson.databind.json.JsonMapper;

/**
 * Round-trip property for the JSON algebra: rendering a {@link JsonValue} to text and parsing it
 * back through {@link JsonValueMapper} recovers a semantically equal value. Lives in the
 * {@code comparison} package to reach the package-private mapper.
 */
class JsonRoundTripProperties {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private static final JsonComparator COMPARATOR = new JsonComparator(Set.of(), false);
    private static final FieldPath ROOT = new FieldPath("/");

    @Provide
    Arbitrary<JsonValue> json() {
        return JsonArbitraries.jsonValues();
    }

    @Property
    void renderThenParseRecoversTheValue(@ForAll("json") JsonValue value) {
        var rendered = JsonValueRenderer.render(value);
        var roundTripped = JsonValueMapper.toDomain(MAPPER.readTree(rendered));
        // The comparator is the equality oracle: numbers compare by value and object key order is
        // irrelevant, which is exactly the equivalence the round-trip must preserve.
        assertThat(COMPARATOR.compare(value, roundTripped, ROOT)).isEmpty();
    }
}
