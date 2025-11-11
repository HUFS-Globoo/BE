package com.Globoo;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@EnableScheduling
@SpringBootApplication
public class GlobooApplication {
    public static void main(String[] args) {
        SpringApplication.run(GlobooApplication.class, args);
    }
}