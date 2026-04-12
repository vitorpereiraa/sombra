package com.github.vitorpereiraa.sombra.domain.comparison;

import static com.google.common.base.Preconditions.checkArgument;

public sealed interface Discrepancy {

    FieldPath path();

    record ValueMismatch(FieldPath path, String expected, String actual) implements Discrepancy {

        public ValueMismatch {
            checkArgument(path != null, "ValueMismatch path cannot be null");
            checkArgument(expected != null, "ValueMismatch expected cannot be null");
            checkArgument(actual != null, "ValueMismatch actual cannot be null");
        }
    }

    record FieldAdded(FieldPath path, String value) implements Discrepancy {

        public FieldAdded {
            checkArgument(path != null, "FieldAdded path cannot be null");
            checkArgument(value != null, "FieldAdded value cannot be null");
        }
    }

    record FieldRemoved(FieldPath path, String value) implements Discrepancy {

        public FieldRemoved {
            checkArgument(path != null, "FieldRemoved path cannot be null");
            checkArgument(value != null, "FieldRemoved value cannot be null");
        }
    }
}
