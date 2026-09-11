package rw.ac.auca.tapwallet.domain;

import org.junit.Test;
import rw.ac.auca.tapwallet.domain.exception.DomainException;
import rw.ac.auca.tapwallet.domain.model.shared.Currency;
import rw.ac.auca.tapwallet.domain.model.shared.Money;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class MoneyTest {

    @Test
    public void addsTwoAmountsOfTheSameCurrency() {
        Money total = Money.of("1500.00", Currency.RWF).plus(Money.of("500.50", Currency.RWF));
        assertEquals(Money.of("2000.50", Currency.RWF), total);
    }

    @Test
    public void subtractsTwoAmountsOfTheSameCurrency() {
        Money left = Money.of("1000.00", Currency.RWF).minus(Money.of("250.00", Currency.RWF));
        assertEquals(Money.of("750.00", Currency.RWF), left);
    }

    @Test
    public void refusesToMixCurrencies() {
        try {
            Money.of("100.00", Currency.RWF).plus(Money.of("100.00", Currency.USD));
            fail("Expected a DomainException");
        } catch (DomainException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void comparesAmounts() {
        assertTrue(Money.of("100.00", Currency.RWF).isLessThan(Money.of("100.01", Currency.RWF)));
        assertFalse(Money.of("100.00", Currency.RWF).isLessThan(Money.of("99.99", Currency.RWF)));
    }

    @Test
    public void isAValueObjectComparedByValueNotIdentity() {
        assertEquals(Money.of("10.00", Currency.RWF), Money.of(new BigDecimal("10.000"), Currency.RWF));
        assertEquals(Money.of("10.00", Currency.RWF).hashCode(), Money.of("10.00", Currency.RWF).hashCode());
        assertNotEquals(Money.of("10.00", Currency.RWF), Money.of("10.00", Currency.USD));
    }

    @Test
    public void roundsToTwoDecimalPlaces() {
        assertEquals(new BigDecimal("10.57"), Money.of("10.567", Currency.RWF).getAmount());
    }

    @Test
    public void knowsItsSign() {
        assertTrue(Money.of("1.00", Currency.RWF).isPositive());
        assertTrue(Money.of("-1.00", Currency.RWF).isNegative());
        assertFalse(Money.zero(Currency.RWF).isPositive());
    }
}
