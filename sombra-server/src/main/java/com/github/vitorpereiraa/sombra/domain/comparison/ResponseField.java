package com.github.vitorpereiraa.sombra.domain.comparison;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

public sealed interface ResponseField {

    record StatusCode() implements ResponseField {}

    record Body(Optional<FieldPath> path) implements ResponseField {

        public Body {
            checkNotNull(path, "Body path Optional cannot be null");
        }

        public Body() {
            this(Optional.empty());
        }

        public Body(FieldPath path) {
            this(Optional.of(path));
        }
    }
}
