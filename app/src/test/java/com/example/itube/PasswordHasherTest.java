package com.example.itube;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;

public class PasswordHasherTest {

    @Test
    public void generatedSaltHasExpectedLength() {
        assertEquals(PasswordHasher.SALT_BYTES, PasswordHasher.generateSalt().length);
    }

    @Test
    public void samePasswordAndSaltProduceSameHash() {
        byte[] salt = new byte[PasswordHasher.SALT_BYTES];
        Arrays.fill(salt, (byte) 7);

        byte[] first = PasswordHasher.derive("secure-password".toCharArray(), salt);
        byte[] second = PasswordHasher.derive("secure-password".toCharArray(), salt);

        assertArrayEquals(first, second);
    }

    @Test
    public void differentPasswordsProduceDifferentHashes() {
        byte[] salt = new byte[PasswordHasher.SALT_BYTES];
        Arrays.fill(salt, (byte) 11);

        byte[] first = PasswordHasher.derive("secure-password".toCharArray(), salt);
        byte[] second = PasswordHasher.derive("different-password".toCharArray(), salt);

        assertFalse(Arrays.equals(first, second));
    }

    @Test
    public void passwordMatchRejectsWrongPassword() {
        byte[] salt = new byte[PasswordHasher.SALT_BYTES];
        Arrays.fill(salt, (byte) 3);
        byte[] expected = PasswordHasher.derive("correct-password".toCharArray(), salt);

        assertTrue(PasswordHasher.matches("correct-password".toCharArray(), salt, expected));
        assertFalse(PasswordHasher.matches("wrong-password".toCharArray(), salt, expected));
    }
}
