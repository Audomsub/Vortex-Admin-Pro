package com.vortexadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VortexAdminProApplication {

	public static void main(String[] args) {
		SpringApplication.run(VortexAdminProApplication.class, args);
	}

}
