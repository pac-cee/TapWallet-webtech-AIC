package rw.ac.auca.tapwallet.model;

import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Set;

import static org.junit.Assert.*;

public class WalletValidationTest {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    private User anyOwner() {
        return new User("Owner", "owner@example.com", "0788123456", "hash", "ACTIVE");
    }

    @Test
    public void aFullyPopulatedWalletHasNoViolations() {
        Wallet wallet = new Wallet(anyOwner(), new BigDecimal("1000.00"), "RWF", "ACTIVE");
        Set<ConstraintViolation<Wallet>> violations = VALIDATOR.validate(wallet);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void aNegativeBalanceIsRejected() {
        Wallet wallet = new Wallet(anyOwner(), new BigDecimal("-5.00"), "RWF", "ACTIVE");
        Set<ConstraintViolation<Wallet>> violations = VALIDATOR.validate(wallet);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void aMissingOwnerIsRejected() {
        Wallet wallet = new Wallet(null, new BigDecimal("10.00"), "RWF", "ACTIVE");
        Set<ConstraintViolation<Wallet>> violations = VALIDATOR.validate(wallet);
        assertFalse(violations.isEmpty());
    }
}
