package com.Globoo;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableAsync
@EnableJpaAuditing
@EnableScheduling
@SpringBootApplication
public class GlobooApplication {
    public static void main(String[] args) {
        SpringApplication.run(GlobooApplication.class, args);
    }
}