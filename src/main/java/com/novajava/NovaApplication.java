package com.novajava;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.novajava.resources")
@EnableJpaRepositories("com.novajava.resources")
public class NovaApplication {
    public static void main(String[] args) {
        SpringApplication.run(NovaApplication.class, args);
    }
}
