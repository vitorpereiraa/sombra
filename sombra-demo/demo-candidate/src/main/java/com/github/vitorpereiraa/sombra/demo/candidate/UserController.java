package com.github.vitorpereiraa.sombra.demo.candidate;

import com.github.vitorpereiraa.sombra.demo.common.UserGenerator;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    /**
     * The "candidate" system: starts from the same deterministic baseline as the original, then
     * lets {@link DivergenceApplier} diverge a controllable fraction of responses. The
     * {@code divergence} rate (0..1) is forwarded by the load test through Sombra's replay, so the
     * demo can dial the proportion of discrepancies up or down.
     */
    @GetMapping("/{id}")
    ResponseEntity<Map<String, Object>> getUser(
            @PathVariable int id,
            @RequestParam(name = "divergence", defaultValue = "0") double divergence) {
        var response = id <= 0
                ? Optional.<Map<String, Object>>empty()
                : DivergenceApplier.apply(UserGenerator.generate(id), id, divergence);
        var status = response.isPresent() ? 200 : 404;
        log.atInfo()
                .addKeyValue("request_method", "GET")
                .addKeyValue("request_path", "/api/users/" + id)
                .addKeyValue("response_status", status)
                .log("GET /api/users/{} -> {}", id, status);
        if (response.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response.get());
    }
}
