package rw.ac.auca.tapwallet.application;

import org.junit.Test;
import rw.ac.auca.tapwallet.domain.exception.DomainException;
import rw.ac.auca.tapwallet.domain.model.shared.AccountStatus;
import rw.ac.auca.tapwallet.domain.model.shared.Currency;
import rw.ac.auca.tapwallet.domain.model.shared.Money;
import rw.ac.auca.tapwallet.domain.model.user.Role;
import rw.ac.auca.tapwallet.domain.model.user.User;
import rw.ac.auca.tapwallet.infrastructure.config.ServiceRegistry;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.Assert.*;

public class UserApplicationServiceTest extends ApplicationServiceTestSupport {

    private final UserApplicationService users = ServiceRegistry.users();

    @Test
    public void registeringACustomerAlsoOpensTheirWallet() {
        String email = uniqueEmail("newcustomer");

        User created = users.register("Alice Uwase", email, "0788123456", "Pass@123",
                Role.CUSTOMER, new BigDecimal("5000.00"), Currency.RWF);

        assertNotNull(created.getId());
        Optional<?> wallet = ServiceRegistry.wallets().findOwnWallet(created.getId());
        assertTrue("a customer must get a wallet in the same transaction", wallet.isPresent());
        assertEquals(Money.of("5000.00", Currency.RWF),
                ServiceRegistry.wallets().findOwnWallet(created.getId()).get().getBalance());
    }

    @Test
    public void registeringAMerchantUserDoesNotOpenAWallet() {
        String email = uniqueEmail("merchantuser");

        User created = users.register("Jean Mugisha", email, "0722123456", "Pass@123",
                Role.MERCHANT, null, Currency.RWF);

        assertFalse(ServiceRegistry.wallets().findOwnWallet(created.getId()).isPresent());
    }

    @Test
    public void theSameEmailCannotBeRegisteredTwice() {
        String email = uniqueEmail("duplicate");
        users.register("First Person", email, "0788123456", "Pass@123", Role.CUSTOMER, BigDecimal.ZERO, Currency.RWF);

        try {
            users.register("Second Person", email, "0788123456", "Pass@123", Role.CUSTOMER, BigDecimal.ZERO, Currency.RWF);
            fail("Expected a DomainException");
        } catch (DomainException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void shortPasswordsAreRefused() {
        try {
            users.register("Weak Password", uniqueEmail("weak"), "0788123456", "123",
                    Role.CUSTOMER, BigDecimal.ZERO, Currency.RWF);
            fail("Expected a DomainException");
        } catch (DomainException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void updatingWithABlankPasswordKeepsTheOldOne() {
        String email = uniqueEmail("keeppass");
        User created = users.register("Keep Password", email, "0788123456", "Pass@123",
                Role.CUSTOMER, BigDecimal.ZERO, Currency.RWF);

        users.update(created.getId(), "Renamed Person", email, "0733123456", "", AccountStatus.ACTIVE);

        User reloaded = users.findById(created.getId()).orElseThrow(IllegalStateException::new);
        assertEquals("Renamed Person", reloaded.getFullName());
        assertTrue(ServiceRegistry.passwordHasher().matches("Pass@123", reloaded.getPasswordHash()));
    }

    @Test
    public void freezingAUserThroughUpdateBlocksSignIn() {
        String email = uniqueEmail("tofreeze");
        User created = users.register("To Freeze", email, "0788123456", "Pass@123",
                Role.CUSTOMER, BigDecimal.ZERO, Currency.RWF);

        users.update(created.getId(), "To Freeze", email, "0788123456", null, AccountStatus.FROZEN);

        User reloaded = users.findById(created.getId()).orElseThrow(IllegalStateException::new);
        assertFalse(reloaded.canSignIn());
    }

    @Test
    public void deletingACustomerAlsoRemovesTheirWallet() {
        User created = users.register("Delete Me", uniqueEmail("deleteme"), "0788123456", "Pass@123",
                Role.CUSTOMER, BigDecimal.ZERO, Currency.RWF);

        users.delete(created.getId());

        assertFalse(users.findById(created.getId()).isPresent());
        assertFalse(ServiceRegistry.wallets().findOwnWallet(created.getId()).isPresent());
    }
}
