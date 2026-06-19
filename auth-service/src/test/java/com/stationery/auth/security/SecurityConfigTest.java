package com.stationery.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pure unit tests for {@link SecurityConfig}.
 * <p>
 * These tests instantiate SecurityConfig directly and exercise its bean
 * methods with mocked collaborators instead of bootstrapping the full
 * Spring application context. This avoids pulling in Eureka discovery,
 * the Spring Cloud Config client, the database, or any other
 * auto-configured infrastructure that {@code @SpringBootTest} would load.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SecurityConfig Unit Tests")
class SecurityConfigTest {

    private SecurityConfig securityConfig;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private HttpSecurity httpSecurity;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @Mock
    private AuthenticationManager authenticationManager;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig();
    }

    @Test
    @DisplayName("passwordEncoder() creates a BCryptPasswordEncoder bean")
    void passwordEncoderBeanIsCreated() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        assertNotNull(encoder, "PasswordEncoder bean should not be null");
        assertInstanceOf(BCryptPasswordEncoder.class, encoder,
                "PasswordEncoder bean should be a BCryptPasswordEncoder");
    }

    @Test
    @DisplayName("passwordEncoder() encodes a raw password into a different value")
    void passwordEncoderEncodesPassword() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String raw = "secretPassword";

        String encoded = encoder.encode(raw);

        assertNotNull(encoded);
        assertNotEquals(raw, encoded, "Encoded password should differ from raw password");
        assertTrue(encoded.startsWith("$2"), "BCrypt hashes start with the $2 prefix");
    }

    @Test
    @DisplayName("passwordEncoder() matches a raw password against its encoded value")
    void passwordEncoderMatchesCorrectPassword() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String raw = "secretPassword";
        String encoded = encoder.encode(raw);

        assertTrue(encoder.matches(raw, encoded), "Matching raw password should return true");
    }

    @Test
    @DisplayName("passwordEncoder() rejects an incorrect password")
    void passwordEncoderRejectsIncorrectPassword() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String encoded = encoder.encode("secretPassword");

        assertFalse(encoder.matches("wrongPassword", encoded), "Non-matching password should return false");
    }

    @Test
    @DisplayName("passwordEncoder() produces different hashes for the same password (salted)")
    void passwordEncoderProducesSaltedHashes() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String raw = "secretPassword";

        String firstEncoded = encoder.encode(raw);
        String secondEncoded = encoder.encode(raw);

        assertNotEquals(firstEncoded, secondEncoded, "BCrypt should salt each hash differently");
        assertTrue(encoder.matches(raw, firstEncoded));
        assertTrue(encoder.matches(raw, secondEncoded));
    }

    @Test
    @DisplayName("authenticationManager() retrieves the manager from AuthenticationConfiguration")
    void authenticationManagerBeanIsCreated() throws Exception {
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);

        AuthenticationManager result = securityConfig.authenticationManager(authenticationConfiguration);

        assertNotNull(result, "AuthenticationManager bean should not be null");
        assertSame(authenticationManager, result);
        verify(authenticationConfiguration, times(1)).getAuthenticationManager();
    }

    @Test
    @DisplayName("securityFilterChain() configures HttpSecurity and returns a filter chain")
    void securityFilterChainBeanIsCreated() throws Exception {
        DefaultSecurityFilterChain expectedChain = new DefaultSecurityFilterChain(request -> true);
        when(httpSecurity.build()).thenReturn(expectedChain);

        SecurityFilterChain result = securityConfig.securityFilterChain(httpSecurity);

        assertNotNull(result, "SecurityFilterChain bean should not be null");
        assertSame(expectedChain, result);

        // SecurityConfig chains .csrf().authorizeHttpRequests().sessionManagement(),
        // then calls .build() on the original httpSecurity reference. With a deep-stub
        // mock, .csrf(...) returns a *different* mock instance, so authorizeHttpRequests()
        // and sessionManagement() are invoked on that child mock, not on httpSecurity
        // itself. Only csrf() and build() are directly observable on httpSecurity.
        verify(httpSecurity, times(1)).csrf(any());
        verify(httpSecurity, times(1)).build();
    }
}