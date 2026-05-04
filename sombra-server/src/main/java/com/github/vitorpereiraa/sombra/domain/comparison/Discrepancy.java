package com.github.vitorpereiraa.sombra.domain.comparison;

import static com.google.common.base.Preconditions.checkArgument;

public sealed interface Discrepancy {

    ResponseField field();

    record ValueMismatch(ResponseField field, DiscrepancyValue originalValue, DiscrepancyValue candidateValue) implements Discrepancy {

        public ValueMismatch {
            checkArgument(field != null, "ValueMismatch field cannot be null");
            checkArgument(originalValue != null, "ValueMismatch originalValue cannot be null");
            checkArgument(candidateValue != null, "ValueMismatch candidateValue cannot be null");
        }
    }

    record FieldAdded(ResponseField field, DiscrepancyValue candidateValue) implements Discrepancy {

        public FieldAdded {
            checkArgument(field != null, "FieldAdded field cannot be null");
            checkArgument(candidateValue != null, "FieldAdded candidateValue cannot be null");
        }
    }

    record FieldRemoved(ResponseField field, DiscrepancyValue originalValue) implements Discrepancy {

        public FieldRemoved {
            checkArgument(field != null, "FieldRemoved field cannot be null");
            checkArgument(originalValue != null, "FieldRemoved originalValue cannot be null");
        }
    }

    record TypeMismatch(ResponseField field, DiscrepancyValue originalValue, DiscrepancyValue candidateValue) implements Discrepancy {

        public TypeMismatch {
            checkArgument(field != null, "TypeMismatch field cannot be null");
            checkArgument(originalValue != null, "TypeMismatch originalValue cannot be null");
            checkArgument(candidateValue != null, "TypeMismatch candidateValue cannot be null");
        }
    }
}
