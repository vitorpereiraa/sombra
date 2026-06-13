package com.github.vitorpereiraa.sombra.comparison;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.vitorpereiraa.sombra.domain.http.HttpBody;
import com.github.vitorpereiraa.sombra.domain.http.HttpHeader;
import com.github.vitorpereiraa.sombra.domain.http.HttpResponse;
import com.github.vitorpereiraa.sombra.domain.http.StatusCode;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Assume;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.IntRange;
import tools.jackson.databind.json.JsonMapper;

class ComparisonServiceProperties {

    private static ComparisonService service() {
        var properties =
                new ComparisonProperties(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        return new ComparisonService(properties, JsonMapper.builder().build());
    }

    private static HttpResponse response(int status, Optional<HttpBody> body) {
        return new HttpResponse(new StatusCode(status), List.of(), body, Duration.ZERO);
    }

    // Two responses with identical status and identical body always match, regardless of whether the
    // body is JSON, plain text, or empty.
    @Property
    void identicalResponsesAlwaysMatch(@ForAll @IntRange(min = 100, max = 599) int status, @ForAll String body) {
        var sameBody = HttpBody.of(body);
        var result = service().compare(response(status, sameBody), response(status, sameBody));
        assertThat(result.matched()).isTrue();
    }

    // A status-code difference always surfaces as a mismatch.
    @Property
    void differingStatusAlwaysMismatches(
            @ForAll @IntRange(min = 100, max = 599) int original, @ForAll @IntRange(min = 100, max = 599) int candidate) {
        Assume.that(original != candidate);
        var result = service().compare(response(original, Optional.empty()), response(candidate, Optional.empty()));
        assertThat(result.matched()).isFalse();
    }

    // toHeaderMap lowercases header names.
    @Property
    void headerMapLowercasesNames(@ForAll("headerNames") String name, @ForAll("headerValues") List<String> values) {
        var map = ComparisonService.toHeaderMap(List.of(new HttpHeader(name, values)));
        assertThat(map).containsOnlyKeys(name.toLowerCase());
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
