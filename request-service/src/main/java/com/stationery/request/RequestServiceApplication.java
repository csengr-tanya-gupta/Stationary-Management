package com.stationery.request;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Main entry point for the request-service microservice.
 *
 * This Spring Boot application registers with Eureka for service discovery,
 * enables Feign clients for remote communication, and starts the request-service REST API.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class RequestServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RequestServiceApplication.class, args);
    }
}
