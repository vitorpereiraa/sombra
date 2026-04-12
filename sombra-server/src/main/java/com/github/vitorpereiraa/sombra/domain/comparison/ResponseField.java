package com.github.vitorpereiraa.sombra.domain.comparison;

import static com.google.common.base.Preconditions.checkArgument;
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

    record Header(String name) implements ResponseField {

        public Header {
            checkArgument(name != null, "Header name cannot be null");
            checkArgument(!name.isBlank(), "Header name cannot be blank");
        }
    }
}
