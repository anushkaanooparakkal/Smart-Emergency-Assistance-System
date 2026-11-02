package com.seas;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

final class Passwords {
    private static final SecureRandom RANDOM = new SecureRandom();
    private Passwords() {}

    static String hash(String password) {
        try {
            byte[] salt = new byte[16]; RANDOM.nextBytes(salt);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 120_000, 256);
            byte[] derived = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            spec.clearPassword();
            return "pbkdf2$120000$" + Base64.getEncoder().withoutPadding().encodeToString(salt) + "$" + Base64.getEncoder().withoutPadding().encodeToString(derived);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not securely store the password.", ex);
        }
    }
}
