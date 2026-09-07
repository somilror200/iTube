package com.example.itube;

import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

final class PasswordHasher {

    static final int ITERATIONS = 120_000;
    static final int KEY_LENGTH_BITS = 256;
    static final int SALT_BYTES = 16;

    private PasswordHasher() {
    }

    static byte[] generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    static byte[] derive(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH_BITS);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return factory.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to derive password hash", e);
        } finally {
            spec.clearPassword();
        }
    }

    static boolean matches(char[] password, byte[] salt, byte[] expectedHash) {
        byte[] actualHash = derive(password, salt);
        return MessageDigest.isEqual(expectedHash, actualHash);
    }
}
