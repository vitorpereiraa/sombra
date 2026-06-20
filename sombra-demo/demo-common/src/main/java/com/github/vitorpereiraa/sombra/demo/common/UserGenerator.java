package com.github.vitorpereiraa.sombra.demo.common;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Deterministically generates a varied-but-reproducible user for a given id. Both demo apps
 * (original and candidate) call this so they share an identical baseline: for any id, the
 * generated user is byte-for-byte the same on both sides. Discrepancies seen by Sombra therefore
 * come only from the candidate's configured divergence, never from generation drift.
 */
public final class UserGenerator {

    private static final List<String> FIRST_NAMES = List.of(
            "Alice", "Bob", "Carol", "David", "Eve", "Frank", "Grace", "Heidi",
            "Ivan", "Judy", "Mallory", "Niaj", "Olivia", "Peggy", "Rupert", "Sybil");

    private static final List<String> ROLES = List.of("admin", "user", "viewer", "editor");

    private static final List<String> CITIES = List.of(
            "Porto", "Lisbon", "Braga", "Coimbra", "Faro", "Aveiro");

    private UserGenerator() {}

    public static Map<String, Object> generate(int id) {
        var random = new Random(id);
        var name = FIRST_NAMES.get(random.nextInt(FIRST_NAMES.size()));
        var role = ROLES.get(random.nextInt(ROLES.size()));
        var city = CITIES.get(random.nextInt(CITIES.size()));
        var signupYear = 2015 + random.nextInt(11);

        var address = new LinkedHashMap<String, Object>();
        address.put("city", city);
        address.put("zip", 1000 + random.nextInt(9000));

        var user = new LinkedHashMap<String, Object>();
        user.put("id", id);
        user.put("name", name);
        user.put("email", name.toLowerCase() + id + "@acme.com");
        user.put("role", role);
        user.put("active", random.nextBoolean());
        user.put("signupYear", signupYear);
        user.put("address", address);
        return user;
    }
}
