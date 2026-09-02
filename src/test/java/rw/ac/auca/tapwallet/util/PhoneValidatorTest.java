package rw.ac.auca.tapwallet.util;

import org.junit.Test;

import javax.faces.validator.ValidatorException;

import static org.junit.Assert.*;

public class PhoneValidatorTest {

    @Test
    public void acceptsAValidMtnNumber() {
        assertTrue(PhoneValidator.isValidPhone("0788123456"));
    }

    @Test
    public void acceptsAValidAirtelNumber() {
        assertTrue(PhoneValidator.isValidPhone("0738123456"));
    }

    @Test
    public void rejectsATooShortNumber() {
        assertFalse(PhoneValidator.isValidPhone("07812345"));
    }

    @Test
    public void rejectsANonRwandanPrefix() {
        assertFalse(PhoneValidator.isValidPhone("0612345678"));
    }

    @Test
    public void rejectsNull() {
        assertFalse(PhoneValidator.isValidPhone(null));
    }

    @Test
    public void validateThrowsForAnInvalidNumber() {
        PhoneValidator validator = new PhoneValidator();
        try {
            validator.validate(null, null, "12345");
            fail("Expected a ValidatorException");
        } catch (ValidatorException ex) {
            assertNotNull(ex.getFacesMessage());
        }
    }

    @Test
    public void validateDoesNotThrowForAValidNumber() {
        PhoneValidator validator = new PhoneValidator();
        validator.validate(null, null, "0788123456");
    }
}
