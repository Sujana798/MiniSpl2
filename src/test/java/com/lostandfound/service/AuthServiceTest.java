package com.lostandfound.service;

import com.lostandfound.dao.UserDao;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private final AuthService authService = new AuthService(new UserDao());

    @Test
    void register_withShortPassword_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.register("Test User", "test_short_pw@example.com", "S001", "123");
        });

        assertTrue(exception.getMessage().contains("at least 6 characters"));
    }

    @Test
    void register_withInvalidEmail_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.register("Test User", "not-an-email", "S001", "password123");
        });

        assertTrue(exception.getMessage().contains("valid email"));
    }

    @Test
    void register_withEmptyName_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            authService.register("", "test2@example.com", "S001", "password123");
        });
    }

    @Test
    void login_withNonExistentEmail_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.login("doesnotexist_xyz@example.com", "somepassword");
        });

        assertEquals("Invalid email or password.", exception.getMessage());
    }
    @Test
    void resetPassword_withNonExistentEmail_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.resetPassword("doesnotexist_xyz@example.com", "SOME-CODE-HERE", "newPassword123");
        });

        assertTrue(exception.getMessage().contains("No account found"));
    }

    @Test
    void resetPassword_withShortNewPassword_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.resetPassword("admin@campus.edu", "SOME-CODE", "123");
        });

        assertTrue(exception.getMessage().contains("at least 6 characters"));
    }
}