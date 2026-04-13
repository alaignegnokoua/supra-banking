package com.suprabanking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class  SupraBankingApplication {

	public static void main(String[] args) {
		SpringApplication.run(SupraBankingApplication.class, args);
	}

}
