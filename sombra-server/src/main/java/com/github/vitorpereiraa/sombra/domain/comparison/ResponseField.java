package com.github.vitorpereiraa.sombra.domain.comparison;

import static com.google.common.base.Preconditions.checkArgument;

public sealed interface ResponseField {

    record StatusCode() implements ResponseField {}

    record Body(FieldPath path) implements ResponseField {

        public Body {
            checkArgument(path != null, "Body path cannot be null");
        }
    }
}
