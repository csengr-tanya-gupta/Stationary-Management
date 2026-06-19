package com.stationery.auth.controller;

import com.stationery.auth.dto.AuthResponse;
import com.stationery.auth.dto.LoginRequest;
import com.stationery.auth.dto.RegisterRequest;
import com.stationery.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication Service", description = "APIs for user authentication and authorization")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user in the system.
     *
     * ENDPOINT PURPOSE: 
     * Allows new users to create an account.
     * 
     * REQUEST FLOW:
     * Receives a POST request with JSON body mapped to RegisterRequest.
     * 
     * RESPONSE FLOW:
     * Returns HTTP 201 Created with an AuthResponse containing a JWT if successful.
     * 
     * SERVICE METHOD CALLED:
     * authService.register(request)
     *
     * @param request the registration request containing username, email, password, and role
     * @return ResponseEntity with the AuthResponse containing the JWT token
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with the provided credentials")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or user already exists")
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Registration request received for username: {}", request.getUsername());
        AuthResponse response = authService.register(request);
        logger.info("Registration successful for username: {}", request.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * ENDPOINT PURPOSE:
     * Allows existing users to sign in and obtain an access token.
     * 
     * REQUEST FLOW:
     * Receives a POST request with JSON body mapped to LoginRequest.
     * 
     * RESPONSE FLOW:
     * Returns HTTP 200 OK with an AuthResponse containing a JWT if credentials are valid.
     * 
     * SERVICE METHOD CALLED:
     * authService.login(request)
     *
     * @param request the login request containing username and password
     * @return ResponseEntity with the AuthResponse containing the JWT token
     */
    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticates user and returns JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Login request received for username: {}", request.getUsername());
        AuthResponse response = authService.login(request);
        logger.info("Login successful for username: {}", request.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Validates a JWT token provided in the Authorization header.
     *
     * ENDPOINT PURPOSE:
     * Used by other microservices or API gateways to verify if a given token is still valid.
     * 
     * REQUEST FLOW:
     * Receives a GET request with an "Authorization: Bearer <token>" header.
     * 
     * RESPONSE FLOW:
     * Returns HTTP 200 OK if valid, or HTTP 401 Unauthorized if invalid or missing.
     * 
     * SERVICE METHOD CALLED:
     * authService.validateToken(token)
     *
     * @param authHeader the Authorization header containing the Bearer token
     * @return ResponseEntity indicating whether the token is valid
     */
    @GetMapping("/validate")
    @Operation(summary = "Validate token", description = "Validates a JWT token provided in the Authorization header")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token is valid"),
            @ApiResponse(responseCode = "401", description = "Token is invalid or expired")
    })
    public ResponseEntity<String> validateToken(@RequestHeader("Authorization") String authHeader) {
        logger.info("Token validation request received");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Token validation failed - invalid Authorization header format");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token format");
        }

        String token = authHeader.substring(7);
        boolean isValid = authService.validateToken(token);

        if (isValid) {
            logger.info("Token validation successful");
            return ResponseEntity.ok("Token is valid");
        } else {
            logger.warn("Token validation failed - token is invalid or expired");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is invalid or expired");
        }
    }
}
