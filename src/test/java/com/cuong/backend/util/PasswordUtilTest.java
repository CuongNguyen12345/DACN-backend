package com.cuong.backend.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {

    @Test
    void encodesAndMatchesBcryptPasswords() {
        String encoded = PasswordUtil.encode("secret-password");

        assertTrue(PasswordUtil.matches("secret-password", encoded));
        assertFalse(PasswordUtil.matches("wrong-password", encoded));
    }
}
