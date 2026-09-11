package rw.ac.auca.tapwallet.domain;

import org.junit.Test;
import rw.ac.auca.tapwallet.domain.exception.DomainException;
import rw.ac.auca.tapwallet.domain.model.merchant.MerchantCode;
import rw.ac.auca.tapwallet.domain.model.user.EmailAddress;
import rw.ac.auca.tapwallet.domain.model.user.PhoneNumber;

import static org.junit.Assert.*;

public class ValueObjectTest {

    @Test
    public void emailIsNormalisedToLowercase() {
        assertEquals("alice@example.com", EmailAddress.of("  Alice@Example.COM ").getValue());
    }

    @Test
    public void emailRejectsRubbish() {
        try {
            EmailAddress.of("not-an-email");
            fail("Expected a DomainException");
        } catch (DomainException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void phoneAcceptsMtnAndAirtelNumbers() {
        assertEquals("0788123456", PhoneNumber.of("0788123456").getValue());
        assertEquals("0738123456", PhoneNumber.of("0738123456").getValue());
    }

    @Test
    public void phoneRejectsWrongPrefixOrLength() {
        assertFalse(PhoneNumber.isValid("0612345678"));
        assertFalse(PhoneNumber.isValid("07812345"));
        assertFalse(PhoneNumber.isValid(null));
    }

    @Test
    public void merchantCodeIsUppercasedAndFormatChecked() {
        assertEquals("CAF-001", MerchantCode.of("caf-001").getValue());
        assertFalse(MerchantCode.isValid("CAFE-1"));
    }

    @Test
    public void valueObjectsAreComparedByValue() {
        assertEquals(EmailAddress.of("a@b.com"), EmailAddress.of("A@B.com"));
        assertEquals(PhoneNumber.of("0788123456"), PhoneNumber.of("0788123456"));
        assertEquals(MerchantCode.of("CAF-001"), MerchantCode.of("caf-001"));
    }
}
