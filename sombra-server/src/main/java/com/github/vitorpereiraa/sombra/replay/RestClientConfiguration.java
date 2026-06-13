package com.github.vitorpereiraa.sombra.replay;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
class RestClientConfiguration {

    @Bean
    RestClient restClient(RestClient.Builder builder, ReplayProperties properties) {
        return builder.baseUrl(properties.candidateUrl()).build();
    }
}
