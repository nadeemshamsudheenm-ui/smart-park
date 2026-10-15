package com.smartpark;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class SmartParkApplication {

    @PostConstruct
    public void init() {
        // Set default timezone to Indian Standard Time (IST)
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
    }

    public static void main(String[] args) {
        SpringApplication.run(SmartParkApplication.class, args);
    }
}
