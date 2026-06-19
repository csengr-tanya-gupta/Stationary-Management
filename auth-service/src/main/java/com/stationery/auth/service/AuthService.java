package com.stationery.auth.service;

import com.stationery.auth.dto.AuthResponse;
import com.stationery.auth.dto.LoginRequest;
import com.stationery.auth.dto.RegisterRequest;
import com.stationery.auth.model.Role;
import com.stationery.auth.model.User;
import com.stationery.auth.repository.UserRepository;
import com.stationery.auth.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Registers a new user in the system.
     * 
     * BUSINESS LOGIC & VALIDATION:
     * - Checks if username already exists. Throws exception if true.
     * - Checks if email already exists. Throws exception if true.
     * - Role Management: Parses the provided role. If invalid, defaults to 'STUDENT' to prevent errors.
     * - Hashes the plain-text password using BCrypt before persisting to the database.
     * - Generates a JWT immediately upon successful registration so the user doesn't need to log in again.
     *
     * @param request the registration request DTO containing user details
     * @return an AuthResponse containing the newly generated JWT token and user details
     * @throws RuntimeException if the username or email is already taken
     */
    public AuthResponse register(RegisterRequest request) {
        logger.info("Attempting to register user: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            logger.warn("Registration failed - username already exists: {}", request.getUsername());
            throw new RuntimeException("Username already exists: " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Registration failed - email already exists: {}", request.getEmail());
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        Role role;
        try {
            role = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid role provided: {}, defaulting to STUDENT", request.getRole());
            role = Role.STUDENT;
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        userRepository.save(user);
        logger.info("User registered successfully: {}", user.getUsername());

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .message("User registered successfully")
                .build();
    }

    /**
     * Authenticates a user and returns a JWT token.
     * 
     * LOGIN PROCESS:
     * 1. Wraps credentials in a UsernamePasswordAuthenticationToken.
     * 2. Passes it to AuthenticationManager, which verifies the password.
     * 3. If successful, fetches the user details from the database.
     * 4. Generates a JWT containing the username and role.
     *
     * @param request the login request DTO containing username and password
     * @return an AuthResponse containing the JWT token and user details
     */
    public AuthResponse login(LoginRequest request) {
        logger.info("Attempting login for user: {}", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        logger.info("User authenticated successfully: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found: " + request.getUsername()));

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .message("Login successful")
                .build();
    }

    /**
     * Validates a JWT token.
     * 
     * WHY IT EXISTS:
     * Other services in our microservices ecosystem (e.g., Inventory, Orders) need to verify 
     * if an incoming request has a valid token. They call this method via the AuthController.
     *
     * @param token the JWT token to validate
     * @return true if the token is valid (not expired, signature matches), false otherwise
     */
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }
}
