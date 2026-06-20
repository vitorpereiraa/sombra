package com.github.vitorpereiraa.sombra.comparison;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.vitorpereiraa.sombra.domain.http.HttpBody;
import com.github.vitorpereiraa.sombra.domain.http.HttpHeader;
import com.github.vitorpereiraa.sombra.domain.http.HttpResponse;
import com.github.vitorpereiraa.sombra.domain.http.StatusCode;
import com.github.vitorpereiraa.sombra.domain.json.JsonArbitraries;
import com.github.vitorpereiraa.sombra.domain.json.JsonValue;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Assume;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.PropertyDefaults;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.IntRange;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

// Each try round-trips bodies through Jackson (serialize + parse), unlike the pure comparator tests,
// so the try count is capped to keep this suite fast; the bounded generators give ample coverage.
@PropertyDefaults(tries = 150)
class ComparisonServiceProperties {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private static final JsonMapper PRETTY_MAPPER =
            JsonMapper.builder().enable(SerializationFeature.INDENT_OUTPUT).build();

    private static ComparisonService service() {
        return service(Optional.empty());
    }

    private static ComparisonService service(Optional<Boolean> compareHeaders) {
        var properties = new ComparisonProperties(Optional.empty(), Optional.empty(), compareHeaders, Optional.empty());
        return new ComparisonService(properties, MAPPER);
    }

    private static HttpResponse response(int status, Optional<HttpBody> body) {
        return response(status, body, List.of());
    }

    private static HttpResponse response(int status, Optional<HttpBody> body, List<HttpHeader> headers) {
        return new HttpResponse(new StatusCode(status), headers, body, Duration.ZERO);
    }

    // Reflexivity: any response always matches itself, whatever its status and body.
    @Property
    void anyResponseMatchesItself(@ForAll("responses") HttpResponse response) {
        assertThat(service().compare(response, response).matched()).isTrue();
    }

    // Symmetry: the verdict never depends on which side is "original" vs "candidate".
    @Property
    void matchIsSymmetric(@ForAll("responses") HttpResponse a, @ForAll("responses") HttpResponse b) {
        var forward = service().compare(a, b).matched();
        var backward = service().compare(b, a).matched();
        assertThat(forward).isEqualTo(backward);
    }

    // The core JSON-awareness guarantee: bodies that are equal JSON match even when their text
    // differs (here, compact vs pretty-printed). A naive string comparison would fail this.
    @Property
    void equalJsonBodiesMatchRegardlessOfFormatting(
            @ForAll @IntRange(min = 100, max = 599) int status, @ForAll("json") JsonValue value) {
        var compact = HttpBody.of(MAPPER.writeValueAsString(value));
        var pretty = HttpBody.of(PRETTY_MAPPER.writeValueAsString(value));
        var result = service().compare(response(status, compact), response(status, pretty));
        assertThat(result.matched()).isTrue();
    }

    // A status-code difference is always reported, regardless of the (shared) body.
    @Property
    void differingStatusAlwaysMismatches(
            @ForAll("responses") HttpResponse response, @ForAll @IntRange(min = 100, max = 599) int otherStatus) {
        Assume.that(response.statusCode().value() != otherStatus);
        var other = response(otherStatus, response.body(), response.headers());
        assertThat(service().compare(response, other).matched()).isFalse();
    }

    // A body present on only one side is always reported, in both directions.
    @Property
    void bodyPresentOnOneSideOnlyMismatches(
            @ForAll @IntRange(min = 100, max = 599) int status, @ForAll("nonEmptyBody") HttpBody body) {
        var withBody = response(status, Optional.of(body));
        var withoutBody = response(status, Optional.empty());
        assertThat(service().compare(withBody, withoutBody).matched()).isFalse();
        assertThat(service().compare(withoutBody, withBody).matched()).isFalse();
    }

    // When header comparison is enabled, header names are matched case-insensitively.
    @Property
    void headerComparisonIsCaseInsensitive(
            @ForAll("headerNames") String name, @ForAll("headerValues") List<String> values) {
        var service = service(Optional.of(true));
        var lower = response(200, Optional.empty(), List.of(new HttpHeader(name.toLowerCase(), values)));
        var upper = response(200, Optional.empty(), List.of(new HttpHeader(name.toUpperCase(), values)));
        assertThat(service.compare(lower, upper).matched()).isTrue();
    }

    // With header comparison disabled (the default), header differences never affect the verdict.
    @Property
    void headerDifferencesAreIgnoredWhenComparisonDisabled(
            @ForAll("headerNames") String name, @ForAll("headerValues") List<String> values) {
        var withHeader = response(200, Optional.empty(), List.of(new HttpHeader(name, values)));
        var withoutHeader = response(200, Optional.empty(), List.of());
        assertThat(service().compare(withHeader, withoutHeader).matched()).isTrue();
    }

    @Provide
    Arbitrary<JsonValue> json() {
        return JsonArbitraries.jsonValues();
    }

    @Provide
    Arbitrary<HttpResponse> responses() {
        return Combinators.combine(Arbitraries.integers().between(100, 599), bodies())
                .as(ComparisonServiceProperties::response);
    }

    @Provide
    Arbitrary<Optional<HttpBody>> bodies() {
        var json = JsonArbitraries.jsonValues().map(value -> HttpBody.of(MAPPER.writeValueAsString(value)));
        var text = Arbitraries.strings().ofMaxLength(10).map(HttpBody::of);
        var empty = Arbitraries.just(Optional.<HttpBody>empty());
        return Arbitraries.oneOf(json, text, empty);
    }

    @Provide
    Arbitrary<HttpBody> nonEmptyBody() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10).map(HttpBody::new);
    }

    @Provide
    Arbitrary<String> headerNames() {
        return Arbitraries.strings()
                .withCharRange('A', 'Z')
                .withCharRange('a', 'z')
                .ofMinLength(1)
                .ofMaxLength(12);
    }

    @Provide
    Arbitrary<List<String>> headerValues() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(8)
                .list()
                .ofMinSize(1)
                .ofMaxSize(3);
    }
}
