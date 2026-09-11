package rw.ac.auca.tapwallet.infrastructure.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Salted SHA-256 implementation of {@link PasswordHasher}. A fresh random salt is
 * generated per password and stored next to the digest as "salt:digest", so two
 * identical passwords never produce the same stored value.
 */
public class Sha256PasswordHasher implements PasswordHasher {

    private static final int SALT_LENGTH = 16;
    private static final String ALGORITHM = "SHA-256";

    @Override
    public String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }
        try {
            byte[] salt = new byte[SALT_LENGTH];
            new SecureRandom().nextBytes(salt);

            byte[] digest = digest(rawPassword, salt);

            return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException failure) {
            throw new IllegalStateException("Unable to hash password", failure);
        }
    }

    @Override
    public boolean matches(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }
        try {
            String[] parts = storedHash.split(":");
            if (parts.length != 2) {
                return false;
            }
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expected = Base64.getDecoder().decode(parts[1]);

            return MessageDigest.isEqual(expected, digest(rawPassword, salt));
        } catch (Exception failure) {
            return false;
        }
    }

    private byte[] digest(String rawPassword, byte[] salt) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest messageDigest = MessageDigest.getInstance(ALGORITHM);
        messageDigest.update(salt);
        return messageDigest.digest(rawPassword.getBytes("UTF-8"));
    }
}
