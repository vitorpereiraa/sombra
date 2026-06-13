package com.github.vitorpereiraa.sombra.domain.comparison;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashSet;
import java.util.Set;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

class FieldPathProperties {

    @Provide
    Arbitrary<String> segments() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withChars("0123456789")
                .ofMinLength(1)
                .ofMaxLength(6);
    }

    @Provide
    Arbitrary<FieldPath> paths() {
        return segments().list().ofMinSize(1).ofMaxSize(4).map(segments -> {
            var path = new FieldPath("/" + segments.getFirst());
            for (var segment : segments.subList(1, segments.size())) {
                path = path.append(segment);
            }
            return path;
        });
    }

    // append always yields a path prefixed by its parent and never introduces a double slash.
    @Property
    void appendIsPrefixedByParent(@ForAll("paths") FieldPath parent, @ForAll("segments") String segment) {
        var child = parent.append(segment);
        assertThat(child.value()).startsWith(parent.value());
        assertThat(child.value()).endsWith("/" + segment);
        assertThat(child.value()).doesNotContain("//");
    }

    // The root "/" is special-cased so appending never produces "//".
    @Property
    void appendOnRootDoesNotDoubleSlash(@ForAll("segments") String segment) {
        var child = new FieldPath("/").append(segment);
        assertThat(child.value()).isEqualTo("/" + segment);
    }

    // A path is ignored by its own value and by any ancestor.
    @Property
    void pathIsIgnoredBySelfAndAncestors(@ForAll("paths") FieldPath path, @ForAll("segments") String childSegment) {
        var child = path.append(childSegment);
        assertThat(child.isIgnoredBy(Set.of(child))).isTrue();
        assertThat(child.isIgnoredBy(Set.of(path))).isTrue();
    }

    // A sibling key that merely shares a string prefix is NOT ignored (e.g. /foobar is not ignored
    // by /foo), thanks to the trailing-slash guard in isIgnoredBy.
    @Property
    void prefixSharingSiblingIsNotIgnored(@ForAll("segments") String base, @ForAll("segments") String suffix) {
        var shorter = new FieldPath("/" + base);
        var longer = new FieldPath("/" + base + suffix);
        assertThat(longer.isIgnoredBy(Set.of(shorter))).isFalse();
    }

    // Ignoring is monotone: enlarging the ignore set never un-ignores a path.
    @Property
    void ignoreIsMonotone(
            @ForAll("paths") FieldPath path, @ForAll("paths") FieldPath ignored, @ForAll("paths") FieldPath extra) {
        if (path.isIgnoredBy(Set.of(ignored))) {
            var enlarged = new HashSet<FieldPath>();
            enlarged.add(ignored);
            enlarged.add(extra);
            assertThat(path.isIgnoredBy(enlarged)).isTrue();
        }
    }

    // Constructor invariants: must start with '/', and reject blank/null values.
    @Property
    void rejectsValuesNotStartingWithSlash(@ForAll("segments") String segment) {
        assertThatThrownBy(() -> new FieldPath(segment)).isInstanceOf(IllegalArgumentException.class);
    }

    @Example
    void rejectsBlankAndNullValues() {
        assertThatThrownBy(() -> new FieldPath("   ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FieldPath(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
