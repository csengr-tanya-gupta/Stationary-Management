package com.stationery.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtUtil Unit Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "abcdefghijklmnopqrstuvwxyz0123456789");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 60_000L);
    }

    @Test
    @DisplayName("Generate token and validate claims successfully")
    void generateTokenThenExtractClaims() {
        String token = jwtUtil.generateToken("alice", "ADMIN");

        assertNotNull(token);
        assertEquals("alice", jwtUtil.extractUsername(token));
        assertEquals("ADMIN", jwtUtil.extractRole(token));
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    @DisplayName("Validate token returns false for malformed token")
    void validateTokenReturnsFalseForMalformedToken() {
        boolean valid = jwtUtil.validateToken("bad-token");

        assertFalse(valid);
    }
}
