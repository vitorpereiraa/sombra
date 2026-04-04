package com.github.vitorpereiraa.sombra.agent;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TestController {

    @PostMapping("/echo")
    String echo(@RequestBody String body) {
        return body;
    }
}
