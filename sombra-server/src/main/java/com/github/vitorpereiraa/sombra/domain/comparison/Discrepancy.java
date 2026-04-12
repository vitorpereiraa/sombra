package com.github.vitorpereiraa.sombra.domain.comparison;

import static com.google.common.base.Preconditions.checkArgument;

public sealed interface Discrepancy {

    ResponseField field();

    record ValueMismatch(ResponseField field) implements Discrepancy {

        public ValueMismatch {
            checkArgument(field != null, "ValueMismatch field cannot be null");
        }
    }

    record FieldAdded(ResponseField field) implements Discrepancy {

        public FieldAdded {
            checkArgument(field != null, "FieldAdded field cannot be null");
        }
    }

    record FieldRemoved(ResponseField field) implements Discrepancy {

        public FieldRemoved {
            checkArgument(field != null, "FieldRemoved field cannot be null");
        }
    }
}
