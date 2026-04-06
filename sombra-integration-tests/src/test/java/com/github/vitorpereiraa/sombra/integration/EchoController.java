package com.github.vitorpereiraa.sombra.integration;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class EchoController {

    private final AtomicInteger callCount = new AtomicInteger();

    @PostMapping("/echo")
    ResponseEntity<String> echo(@RequestBody String body) {
        callCount.incrementAndGet();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/echo/count")
    ResponseEntity<Integer> count() {
        return ResponseEntity.ok(callCount.get());
    }
}
