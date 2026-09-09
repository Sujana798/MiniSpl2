package com.lostandfound.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void hashPassword_shouldNotReturnPlainText() {
        String plain = "mySecret123";
        String hashed = PasswordUtil.hashPassword(plain);

        assertNotEquals(plain, hashed);
        assertTrue(hashed.length() > 20);
    }

    @Test
    void checkPassword_withCorrectPassword_shouldReturnTrue() {
        String plain = "mySecret123";
        String hashed = PasswordUtil.hashPassword(plain);

        assertTrue(PasswordUtil.checkPassword(plain, hashed));
    }

    @Test
    void checkPassword_withWrongPassword_shouldReturnFalse() {
        String hashed = PasswordUtil.hashPassword("mySecret123");

        assertFalse(PasswordUtil.checkPassword("wrongPassword", hashed));
    }

    @Test
    void hashPassword_calledTwiceWithSameInput_shouldProduceDifferentHashes() {
        String plain = "mySecret123";
        String hash1 = PasswordUtil.hashPassword(plain);
        String hash2 = PasswordUtil.hashPassword(plain);

        assertNotEquals(hash1, hash2, "BCrypt should use a random salt each time");
    }
}