package com.beepollen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Bee Pollen &amp; Plant Management System.
 *
 * <p>This Spring Boot application provides APIs and a Thymeleaf-based UI
 * for managing plant species, pollen data, bee colonies, and
 * pollen-collection tracking records.</p>
 */
@SpringBootApplication
@EnableScheduling
public class BeePollenManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeePollenManagementApplication.class, args);
    }
}
