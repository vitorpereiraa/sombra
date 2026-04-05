package com.github.vitorpereiraa.sombra.integration;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class EchoController {

    @PostMapping("/echo")
    ResponseEntity<String> echo(@RequestBody String body) {
        return ResponseEntity.ok(body);
    }
}
