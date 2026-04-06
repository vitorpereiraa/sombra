package com.github.vitorpereiraa.sombra;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
class RestClientConfiguration {

    @Bean
    RestClient restClient(RestClient.Builder builder) {
        return builder.build();
    }
}
