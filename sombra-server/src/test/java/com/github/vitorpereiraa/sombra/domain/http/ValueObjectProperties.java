package com.github.vitorpereiraa.sombra.domain.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.vitorpereiraa.sombra.domain.capture.TraceId;
import java.util.List;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Assume;
import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.IntRange;

class ValueObjectProperties {

    // StatusCode: accepts 100..599 and classifies into 1xx..5xx.
    @Property
    void statusCodeAcceptsValidRangeAndClassifies(@ForAll @IntRange(min = 100, max = 599) int value) {
        var status = new StatusCode(value);
        assertThat(status.statusClass()).isEqualTo((value / 100) + "xx");
        assertThat(status.statusClass()).matches("[1-5]xx");
    }

    @Property
    void statusCodeRejectsOutOfRange(@ForAll int value) {
        Assume.that(value < 100 || value > 599);
        assertThatThrownBy(() -> new StatusCode(value)).isInstanceOf(IllegalArgumentException.class);
    }

    // HttpMethod: name round-trips, and lookup is case-insensitive and null/garbage-safe.
    @Property
    void httpMethodRoundTripsByName(@ForAll HttpMethod method) {
        assertThat(HttpMethod.from(method.name())).isEqualTo(method);
        assertThat(HttpMethod.of(method.name().toLowerCase())).contains(method);
    }

    @Property
    void httpMethodRejectsUnknownNames(@ForAll("nonMethod") String value) {
        assertThat(HttpMethod.of(value)).isEmpty();
    }

    @Example
    void httpMethodOfHandlesNull() {
        assertThat(HttpMethod.of(null)).isEmpty();
    }

    // HttpBody.of is present exactly when the constructor would accept the content (non-blank).
    @Property
    void httpBodyOfIsPresentIffConstructible(@ForAll String content) {
        if (content.isBlank()) {
            assertThat(HttpBody.of(content)).isEmpty();
        } else {
            assertThat(HttpBody.of(content)).contains(new HttpBody(content));
        }
    }

    // Blank-or-null rejection across the simple string value objects.
    @Property
    void requestPathRejectsBlank(@ForAll("blank") String blank) {
        assertThatThrownBy(() -> new RequestPath(blank)).isInstanceOf(IllegalArgumentException.class);
    }

    @Property
    void traceIdRejectsBlank(@ForAll("blank") String blank) {
        assertThatThrownBy(() -> new TraceId(blank)).isInstanceOf(IllegalArgumentException.class);
    }

    // HttpHeader rejects an empty value list and a blank name.
    @Property
    void httpHeaderRejectsEmptyValues(@ForAll("nonBlank") String name) {
        assertThatThrownBy(() -> new HttpHeader(name, List.of())).isInstanceOf(IllegalArgumentException.class);
    }

    @Property
    void httpHeaderRejectsBlankName(@ForAll("nonBlank") String value) {
        assertThatThrownBy(() -> new HttpHeader("   ", List.of(value))).isInstanceOf(IllegalArgumentException.class);
    }

    @Provide
    Arbitrary<String> nonMethod() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(8).filter(s -> HttpMethod.of(s)
                .isEmpty());
    }

    @Provide
    Arbitrary<String> blank() {
        return Arbitraries.of("", " ", "   ", "\t", "\n", "\t \n");
    }

    @Provide
    Arbitrary<String> nonBlank() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(8);
    }
}
