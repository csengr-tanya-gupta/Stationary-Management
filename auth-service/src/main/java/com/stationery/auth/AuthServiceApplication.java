package com.stationery.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main application class for the Auth Service.
 * 
 * WHY IT EXISTS:
 * This is the bootstrap class for the Spring Boot microservice. It contains the main method 
 * which is the entry point of the Java application.
 * 
 * ARCHITECTURE & ROLE IN ECOSYSTEM:
 * - This service handles identity and access management (Registration, Login, JWT issuing).
 * - @EnableDiscoveryClient: Registers this service with Eureka (or another discovery server) 
 *   so other microservices and the API Gateway can locate it dynamically.
 * - @SpringBootApplication: Triggers component scanning, auto-configuration, and property support.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
