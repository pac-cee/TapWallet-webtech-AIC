package rw.ac.auca.tapwallet.infrastructure;

import org.junit.Test;
import rw.ac.auca.tapwallet.infrastructure.security.PasswordHasher;
import rw.ac.auca.tapwallet.infrastructure.security.Sha256PasswordHasher;

import static org.junit.Assert.*;

public class Sha256PasswordHasherTest {

    private final PasswordHasher hasher = new Sha256PasswordHasher();

    @Test
    public void neverStoresThePlainPassword() {
        String stored = hasher.hash("Secret@123");
        assertFalse(stored.contains("Secret@123"));
        assertTrue(stored.contains(":"));
    }

    @Test
    public void acceptsTheRightPassword() {
        assertTrue(hasher.matches("Secret@123", hasher.hash("Secret@123")));
    }

    @Test
    public void rejectsTheWrongPassword() {
        assertFalse(hasher.matches("WrongPassword", hasher.hash("Secret@123")));
    }

    @Test
    public void saltsEachHashSoTwoIdenticalPasswordsLookDifferent() {
        assertNotEquals(hasher.hash("Secret@123"), hasher.hash("Secret@123"));
    }

    @Test
    public void rejectsGarbageStoredValues() {
        assertFalse(hasher.matches("Secret@123", "not-a-real-hash"));
        assertFalse(hasher.matches("Secret@123", null));
    }
}
