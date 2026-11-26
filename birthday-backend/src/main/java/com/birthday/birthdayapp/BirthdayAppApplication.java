package com.birthday.birthdayapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BirthdayAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(BirthdayAppApplication.class, args);
	}

}
