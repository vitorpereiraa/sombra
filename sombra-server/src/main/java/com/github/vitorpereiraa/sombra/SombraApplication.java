package com.github.vitorpereiraa.sombra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SombraServerProperties.class)
public class SombraApplication {

	static void main(String[] args) {
		SpringApplication.run(SombraApplication.class, args);
	}

}
