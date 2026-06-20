package com.github.vitorpereiraa.sombra.demo.candidate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Decides, per request, whether the candidate response should diverge from the original baseline,
 * and if so how. The decision is seeded by the user id, so it is reproducible, while the
 * {@code divergence} rate (0..1) controls the probability of divergence: at 0 the candidate always
 * matches the original, at 1 it always diverges. The mutation category rotates through every
 * discrepancy type Sombra can report.
 *
 * <p>An empty result signals a status mismatch (the candidate responds 404 where the original
 * responded 200). A present result is the 200 body to return.
 */
final class DivergenceApplier {

    private DivergenceApplier() {}

    static Optional<Map<String, Object>> apply(Map<String, Object> baseline, int id, double divergence) {
        var random = new Random(id ^ 0x9E3779B9L);
        if (random.nextDouble() >= divergence) {
            return Optional.of(baseline);
        }
        var mutated = new LinkedHashMap<String, Object>(baseline);
        return switch (random.nextInt(5)) {
            case 0 -> { // value mismatch: same field, different value
                mutated.put("email", "changed+" + mutated.get("email"));
                yield Optional.of(mutated);
            }
            case 1 -> { // field added: candidate has an extra field
                mutated.put("department", "engineering");
                yield Optional.of(mutated);
            }
            case 2 -> { // field removed: candidate drops a field
                mutated.remove("role");
                yield Optional.of(mutated);
            }
            case 3 -> { // type mismatch: number becomes a string
                mutated.put("signupYear", String.valueOf(mutated.get("signupYear")));
                yield Optional.of(mutated);
            }
            default -> Optional.empty(); // status mismatch: 404 instead of 200
        };
    }
}
