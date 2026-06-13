package com.github.vitorpereiraa.sombra;

import com.github.vitorpereiraa.sombra.comparison.ComparisonProperties;
import com.github.vitorpereiraa.sombra.ingestion.IngestionProperties;
import com.github.vitorpereiraa.sombra.replay.ReplayProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({IngestionProperties.class, ReplayProperties.class, ComparisonProperties.class})
public class SombraServerApplication {

	static void main(String[] args) {
		SpringApplication.run(SombraServerApplication.class, args);
	}

}
