package com.company.runcoach.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.company.runcoach")
public class RunCoachApplication {

    public static void main(String[] args) {
        SpringApplication.run(RunCoachApplication.class, args);
    }
}
