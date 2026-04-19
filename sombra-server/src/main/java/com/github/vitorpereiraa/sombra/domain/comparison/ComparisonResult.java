package com.github.vitorpereiraa.sombra.domain.comparison;

import static com.google.common.base.Preconditions.checkArgument;

import com.github.vitorpereiraa.sombra.domain.http.HttpResponse;

import java.util.List;

public record ComparisonResult(
    HttpResponse originalResponse,
    HttpResponse candidateResponse,
    List<Discrepancy> discrepancies
) {

    public ComparisonResult {
        checkArgument(originalResponse != null, "ComparisonResult original cannot be null");
        checkArgument(candidateResponse != null, "ComparisonResult candidate cannot be null");
        checkArgument(discrepancies != null, "ComparisonResult discrepancies cannot be null");
        discrepancies = List.copyOf(discrepancies);
    }

    public boolean matched() {
        return discrepancies.isEmpty();
    }
}
