package com.github.vitorpereiraa.sombra.demo.candidate;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
class UserController {

    private static final Map<Integer, Map<String, Object>> USERS = Map.of(
            1, Map.of("id", 1, "name", "Alice", "email", "alice@acme.com", "role", "admin"),
            2, candidateUser2(),
            999, Map.of("id", 999, "name", "Ghost", "email", "ghost@acme.com", "role", "viewer"));

    private static Map<String, Object> candidateUser2() {
        var user = new LinkedHashMap<String, Object>();
        user.put("id", 2);
        user.put("name", "Robert");
        user.put("email", "bob@newdomain.com");
        user.put("role", "user");
        user.put("department", "engineering");
        return Map.copyOf(user);
    }

    @GetMapping("/{id}")
    ResponseEntity<Map<String, Object>> getUser(@PathVariable int id) {
        var user = USERS.get(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }
}
