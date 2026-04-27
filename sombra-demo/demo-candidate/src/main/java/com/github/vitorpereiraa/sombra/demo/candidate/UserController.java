package com.github.vitorpereiraa.sombra.demo.candidate;

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
            2, Map.of("id", 2, "name", "Robert", "email", "bob@newdomain.com",
                    "role", "user", "department", "engineering"),
            999, Map.of("id", 999, "name", "Ghost", "email", "ghost@acme.com", "role", "viewer"));

    @GetMapping("/{id}")
    ResponseEntity<Map<String, Object>> getUser(@PathVariable int id) {
        var user = USERS.get(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }
}
