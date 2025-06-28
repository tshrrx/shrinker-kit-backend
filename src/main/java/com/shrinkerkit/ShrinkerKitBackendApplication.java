package com.shrinkerkit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// By placing this class in the root package, Spring Boot will automatically
// scan all sub-packages (like .controller, .service, .repository) by default.
@SpringBootApplication
public class ShrinkerKitBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShrinkerKitBackendApplication.class, args);
    }

}