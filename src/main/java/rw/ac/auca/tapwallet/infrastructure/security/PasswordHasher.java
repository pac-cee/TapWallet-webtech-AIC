package rw.ac.auca.tapwallet.infrastructure.security;

/**
 * Strategy pattern. The application depends on this contract, so the hashing
 * algorithm can be swapped (for example for BCrypt) without touching any caller.
 */
public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String storedHash);
}
