package com.dev.vault;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
// TODO: Refactor the services. the codes are way too chunky and long.
@EnableScheduling
@EnableReactiveMongoAuditing // for enabling @CreatedDate
public class DevVaultReactiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevVaultReactiveApplication.class, args);
    }

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}
}
