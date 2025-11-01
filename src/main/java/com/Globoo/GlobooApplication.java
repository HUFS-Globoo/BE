package com.Globoo;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class GlobooApplication {
    public static void main(String[] args) {
        SpringApplication.run(GlobooApplication.class, args);
    }
}
