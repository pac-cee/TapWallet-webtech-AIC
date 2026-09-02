package rw.ac.auca.tapwallet.model;

import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.*;

public class UserValidationTest {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    @Test
    public void aFullyPopulatedUserHasNoViolations() {
        User user = new User("Pacifique Bakundukize", "pacifique@example.com", "0788123456", "hash", "CUSTOMER", "ACTIVE");
        Set<ConstraintViolation<User>> violations = VALIDATOR.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void aBlankFullNameIsRejected() {
        User user = new User("", "pacifique@example.com", "0788123456", "hash", "CUSTOMER", "ACTIVE");
        Set<ConstraintViolation<User>> violations = VALIDATOR.validate(user);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void anInvalidEmailIsRejected() {
        User user = new User("Pacifique Bakundukize", "not-an-email", "0788123456", "hash", "CUSTOMER", "ACTIVE");
        Set<ConstraintViolation<User>> violations = VALIDATOR.validate(user);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void aBlankPasswordHashIsRejected() {
        User user = new User("Pacifique Bakundukize", "pacifique@example.com", "0788123456", "", "CUSTOMER", "ACTIVE");
        Set<ConstraintViolation<User>> violations = VALIDATOR.validate(user);
        assertFalse(violations.isEmpty());
    }
}
