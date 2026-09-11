package rw.ac.auca.tapwallet.domain;

import org.junit.Before;
import org.junit.Test;
import rw.ac.auca.tapwallet.domain.exception.DomainException;
import rw.ac.auca.tapwallet.domain.exception.InactiveAccountException;
import rw.ac.auca.tapwallet.domain.exception.InsufficientFundsException;
import rw.ac.auca.tapwallet.domain.model.shared.Currency;
import rw.ac.auca.tapwallet.domain.model.shared.Money;
import rw.ac.auca.tapwallet.domain.model.user.EmailAddress;
import rw.ac.auca.tapwallet.domain.model.user.PhoneNumber;
import rw.ac.auca.tapwallet.domain.model.user.Role;
import rw.ac.auca.tapwallet.domain.model.user.User;
import rw.ac.auca.tapwallet.domain.model.wallet.Wallet;

import static org.junit.Assert.*;

public class WalletTest {

    private Wallet wallet;

    @Before
    public void openWallet() {
        User owner = User.register("Alice Uwase", EmailAddress.of("alice@example.com"),
                PhoneNumber.of("0788123456"), "hash", Role.CUSTOMER);
        wallet = Wallet.openFor(owner, Money.of("1000.00", Currency.RWF));
    }

    @Test
    public void creditIncreasesTheBalance() {
        wallet.credit(Money.of("500.00", Currency.RWF));
        assertEquals(Money.of("1500.00", Currency.RWF), wallet.getBalance());
    }

    @Test
    public void debitDecreasesTheBalance() {
        wallet.debit(Money.of("400.00", Currency.RWF));
        assertEquals(Money.of("600.00", Currency.RWF), wallet.getBalance());
    }

    @Test
    public void debitBeyondTheBalanceIsRefusedAndChangesNothing() {
        try {
            wallet.debit(Money.of("5000.00", Currency.RWF));
            fail("Expected an InsufficientFundsException");
        } catch (InsufficientFundsException expected) {
            assertNotNull(expected.getMessage());
        }
        assertEquals(Money.of("1000.00", Currency.RWF), wallet.getBalance());
    }

    @Test
    public void aFrozenWalletCannotMoveMoney() {
        wallet.freeze();
        try {
            wallet.debit(Money.of("10.00", Currency.RWF));
            fail("Expected an InactiveAccountException");
        } catch (InactiveAccountException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void zeroOrNegativeAmountsAreRefused() {
        try {
            wallet.debit(Money.zero(Currency.RWF));
            fail("Expected a DomainException");
        } catch (DomainException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void aWalletRefusesAmountsInAnotherCurrency() {
        try {
            wallet.credit(Money.of("10.00", Currency.USD));
            fail("Expected a DomainException");
        } catch (DomainException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void openingBalanceCannotBeNegative() {
        User owner = User.register("Eric N", EmailAddress.of("eric@example.com"),
                PhoneNumber.of("0788123456"), "hash", Role.CUSTOMER);
        try {
            Wallet.openFor(owner, Money.of("-1.00", Currency.RWF));
            fail("Expected a DomainException");
        } catch (DomainException expected) {
            assertNotNull(expected.getMessage());
        }
    }
}
