package com.stationery.auth.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Model and Role Enum Tests")
class UserRoleModelTest {

    @Test
    void userBuilderAndAccessorsShouldWork() {
        User user = User.builder()
                .id(42L)
                .username("student")
                .email("student@example.com")
                .password("password")
                .role(Role.STUDENT)
                .build();

        assertEquals(42L, user.getId());
        assertEquals("student", user.getUsername());
        assertEquals("student@example.com", user.getEmail());
        assertEquals("password", user.getPassword());
        assertEquals(Role.STUDENT, user.getRole());

        user.setUsername("newuser");
        user.setEmail("new@example.com");
        user.setPassword("newpassword");
        user.setRole(Role.ADMIN);

        assertEquals("newuser", user.getUsername());
        assertEquals("new@example.com", user.getEmail());
        assertEquals("newpassword", user.getPassword());
        assertEquals(Role.ADMIN, user.getRole());
    }

    @Test
    void prePersistAndPreUpdateShouldSetTimestamps() {
        User user = User.builder()
                .username("student")
                .email("student@example.com")
                .password("password")
                .role(Role.STUDENT)
                .build();

        user.onCreate();
        LocalDateTime createdAt = user.getCreatedAt();
        LocalDateTime updatedAt = user.getUpdatedAt();

        assertNotNull(createdAt);
        assertNotNull(updatedAt);
        assertFalse(updatedAt.isBefore(createdAt));

        user.onUpdate();
        assertNotNull(user.getUpdatedAt());
        assertFalse(user.getUpdatedAt().isBefore(updatedAt));
    }

    @Test
    void roleEnumShouldContainAdminAndStudent() {
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
        assertEquals(Role.STUDENT, Role.valueOf("STUDENT"));
    }
}
