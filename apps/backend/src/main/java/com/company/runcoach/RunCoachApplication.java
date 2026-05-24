package com.company.runcoach;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.company.runcoach")
@EnableJpaRepositories(basePackages = "com.company.runcoach")
@EntityScan(basePackages = "com.company.runcoach")
public class RunCoachApplication {

    public static void main(String[] args) {
        SpringApplication.run(RunCoachApplication.class, args);
    }
}
