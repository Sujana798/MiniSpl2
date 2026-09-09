package com.lostandfound.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RecoveryCodeUtilTest {

    @Test
    void generateRecoveryCode_shouldMatchExpectedFormat() {
        String code = RecoveryCodeUtil.generateRecoveryCode();

        assertNotNull(code);
        assertTrue(code.matches("^[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}$"),
                "Recovery code should match format XXXX-XXXX-XXXX");
    }

    @Test
    void generateRecoveryCode_shouldNotContainConfusingCharacters() {
        String code = RecoveryCodeUtil.generateRecoveryCode();

        assertFalse(code.contains("O"));
        assertFalse(code.contains("0"));
        assertFalse(code.contains("I"));
        assertFalse(code.contains("1"));
    }

    @Test
    void generateRecoveryCode_calledTwice_shouldProduceDifferentCodes() {
        String code1 = RecoveryCodeUtil.generateRecoveryCode();
        String code2 = RecoveryCodeUtil.generateRecoveryCode();

        assertNotEquals(code1, code2, "Two generated codes should very unlikely be the same");
    }
}