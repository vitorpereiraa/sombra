package com.github.vitorpereiraa.sombra.integration;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
class EchoController {

    private final AtomicInteger replayCallCount = new AtomicInteger();

    @PostMapping("/echo")
    ResponseEntity<String> echo(@RequestBody String body) {
        return ResponseEntity.ok(body);
    }

    @PostMapping("/echo/replay")
    ResponseEntity<String> echoReplay(@RequestBody String body) {
        replayCallCount.incrementAndGet();
        return ResponseEntity.ok(body);
    }

    @PostMapping("/echo/different")
    ResponseEntity<String> echoDifferent(
            @RequestBody String body,
            @RequestHeader(value = "X-Sombra-Replay", required = false) String replayHeader) {
        if (replayHeader != null) {
            return ResponseEntity.ok("{\"name\":\"changed\",\"value\":42}");
        }
        return ResponseEntity.ok(body);
    }

    @GetMapping("/echo/replay/count")
    ResponseEntity<Integer> replayCount() {
        return ResponseEntity.ok(replayCallCount.get());
    }
}
