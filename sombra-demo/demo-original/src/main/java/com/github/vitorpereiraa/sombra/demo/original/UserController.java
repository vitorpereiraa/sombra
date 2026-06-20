package com.github.vitorpereiraa.sombra.demo.original;

import com.github.vitorpereiraa.sombra.demo.common.UserGenerator;
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

    /**
     * The "current" system: always returns the deterministic baseline for an id. It ignores any
     * {@code divergence} query parameter the load test forwards; only the candidate honors it.
     */
    @GetMapping("/{id}")
    ResponseEntity<Map<String, Object>> getUser(@PathVariable int id) {
        var status = id <= 0 ? 404 : 200;
        log.atInfo()
                .addKeyValue("request_method", "GET")
                .addKeyValue("request_path", "/api/users/" + id)
                .addKeyValue("response_status", status)
                .log("GET /api/users/{} -> {}", id, status);
        if (id <= 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(UserGenerator.generate(id));
    }
}
