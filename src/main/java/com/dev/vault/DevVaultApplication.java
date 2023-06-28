package com.dev.vault;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
// TODO: Refactor the services. the codes are way too chunky and long.
// TODO: There are a few places that have been using wrong exception signatures, refactor them and use proper exceptions
//  (DevVaultException -> NotMemberOfProjectException)
@EnableScheduling
public class DevVaultApplication {

	public static void main(String[] args) {
		SpringApplication.run(DevVaultApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}
}
