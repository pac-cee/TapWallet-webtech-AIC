package rw.ac.auca.tapwallet.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class PasswordUtilTest {

    @Test
    public void hashProducesSaltColonHashFormat() {
        String hash = PasswordUtil.hash("Secret123");
        assertTrue(hash.contains(":"));
    }

    @Test
    public void verifyAcceptsTheCorrectPassword() {
        String hash = PasswordUtil.hash("Secret123");
        assertTrue(PasswordUtil.verify("Secret123", hash));
    }

    @Test
    public void verifyRejectsTheWrongPassword() {
        String hash = PasswordUtil.hash("Secret123");
        assertFalse(PasswordUtil.verify("WrongPassword", hash));
    }

    @Test
    public void twoHashesOfTheSamePasswordAreDifferent() {
        String hash1 = PasswordUtil.hash("Secret123");
        String hash2 = PasswordUtil.hash("Secret123");
        assertNotEquals(hash1, hash2);
    }
}
