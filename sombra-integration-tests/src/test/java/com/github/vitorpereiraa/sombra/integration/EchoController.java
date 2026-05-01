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

    private final AtomicInteger callCount = new AtomicInteger();
    private final AtomicInteger replayCallCount = new AtomicInteger();

    @PostMapping("/echo")
    ResponseEntity<String> echo(@RequestBody String body) {
        callCount.incrementAndGet();
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
        callCount.incrementAndGet();
        if (replayHeader != null) {
            return ResponseEntity.ok("{\"name\":\"changed\",\"value\":42}");
        }
        return ResponseEntity.ok(body);
    }

    @PostMapping("/echo/array-reordered")
    ResponseEntity<String> echoArrayReordered(
            @RequestBody String body,
            @RequestHeader(value = "X-Sombra-Replay", required = false) String replayHeader) {
        callCount.incrementAndGet();
        if (replayHeader != null) {
            return ResponseEntity.ok("[{\"id\":2},{\"id\":1}]");
        }
        return ResponseEntity.ok(body);
    }

    @PostMapping("/echo/non-json")
    ResponseEntity<String> echoNonJson(
            @RequestBody String body,
            @RequestHeader(value = "X-Sombra-Replay", required = false) String replayHeader) {
        callCount.incrementAndGet();
        if (replayHeader != null) {
            return ResponseEntity.ok("not json at all");
        }
        return ResponseEntity.ok(body);
    }

    @GetMapping("/echo/count")
    ResponseEntity<Integer> count() {
        return ResponseEntity.ok(callCount.get());
    }

    @GetMapping("/echo/replay/count")
    ResponseEntity<Integer> replayCount() {
        return ResponseEntity.ok(replayCallCount.get());
    }
}
