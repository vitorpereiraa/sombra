package com.github.vitorpereiraa.sombra.domain.comparison;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;

public record ComparisonResult(List<Discrepancy> discrepancies) {

    public ComparisonResult {
        checkArgument(discrepancies != null, "ComparisonResult discrepancies cannot be null");
        discrepancies = List.copyOf(discrepancies);
    }

    public boolean matched() {
        return discrepancies.isEmpty();
    }
}
