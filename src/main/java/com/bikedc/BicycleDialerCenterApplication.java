package com.bikedc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BicycleDialerCenterApplication {
    public static void main(String[] args) {
        SpringApplication.run(BicycleDialerCenterApplication.class, args);
    }
}