package com.github.vitorpereiraa.sombra.demo.original;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private static final Map<Integer, Map<String, Object>> USERS = Map.of(
            1, Map.of("id", 1, "name", "Alice", "email", "alice@acme.com", "role", "admin"),
            2, Map.of("id", 2, "name", "Bob", "email", "bob@acme.com", "role", "user"));

    @GetMapping("/{id}")
    ResponseEntity<Map<String, Object>> getUser(@PathVariable int id) {
        var user = USERS.get(id);
        var status = user == null ? 404 : 200;
        log.atInfo()
                .addKeyValue("request_method", "GET")
                .addKeyValue("request_path", "/api/users/" + id)
                .addKeyValue("response_status", status)
                .log("GET /api/users/{} -> {}", id, status);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }
}
