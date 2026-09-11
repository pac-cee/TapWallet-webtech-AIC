package rw.ac.auca.tapwallet.application;

import org.junit.Test;
import rw.ac.auca.tapwallet.domain.exception.DomainException;
import rw.ac.auca.tapwallet.domain.model.merchant.Merchant;
import rw.ac.auca.tapwallet.domain.model.shared.AccountStatus;
import rw.ac.auca.tapwallet.domain.model.shared.Currency;
import rw.ac.auca.tapwallet.domain.model.user.Role;
import rw.ac.auca.tapwallet.domain.model.user.User;
import rw.ac.auca.tapwallet.infrastructure.config.ServiceRegistry;

import static org.junit.Assert.*;

public class MerchantApplicationServiceTest extends ApplicationServiceTestSupport {

    private final MerchantApplicationService merchants = ServiceRegistry.merchants();

    @Test
    public void registersAShopForAMerchantUser() {
        User operator = createUser("Jean Mugisha", uniqueEmail("regop"), Role.MERCHANT, "Pass@123");

        Merchant shop = merchants.register("AUCA Cafeteria", uniqueMerchantCode(), operator.getId(), Currency.RWF);

        assertNotNull(shop.getId());
        assertTrue(shop.isOperatedBy(operator.getId()));
        assertTrue(shop.isActive());
    }

    @Test
    public void refusesToMakeACustomerAShopOperator() {
        User customer = createUser("Alice Customer", uniqueEmail("notmerchant"), Role.CUSTOMER, "Pass@123");

        try {
            merchants.register("Wrong Role Shop", uniqueMerchantCode(), customer.getId(), Currency.RWF);
            fail("Expected a DomainException");
        } catch (DomainException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void oneOperatorCannotRunTwoShops() {
        User operator = createUser("Busy Operator", uniqueEmail("busyop"), Role.MERCHANT, "Pass@123");
        merchants.register("First Shop", uniqueMerchantCode(), operator.getId(), Currency.RWF);

        try {
            merchants.register("Second Shop", uniqueMerchantCode(), operator.getId(), Currency.RWF);
            fail("Expected a DomainException");
        } catch (DomainException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void merchantCodesAreUnique() {
        String sharedCode = uniqueMerchantCode();
        User first = createUser("First Operator", uniqueEmail("code1"), Role.MERCHANT, "Pass@123");
        User second = createUser("Second Operator", uniqueEmail("code2"), Role.MERCHANT, "Pass@123");
        merchants.register("First Shop", sharedCode, first.getId(), Currency.RWF);

        try {
            merchants.register("Second Shop", sharedCode, second.getId(), Currency.RWF);
            fail("Expected a DomainException");
        } catch (DomainException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void freezingAShopRemovesItFromTheListCustomersCanPay() {
        User operator = createUser("Freeze Operator", uniqueEmail("freezeop"), Role.MERCHANT, "Pass@123");
        Merchant shop = merchants.register("Freezable Shop", uniqueMerchantCode(), operator.getId(), Currency.RWF);

        merchants.update(shop.getId(), "Freezable Shop", shop.getCode().getValue(), AccountStatus.FROZEN);

        boolean stillPayable = merchants.findActive().stream()
                .anyMatch(m -> m.getId().equals(shop.getId()));
        assertFalse(stillPayable);
    }

    @Test
    public void aMerchantOnlyResolvesTheirOwnShop() {
        User mine = createUser("My Operator", uniqueEmail("mineop"), Role.MERCHANT, "Pass@123");
        User theirs = createUser("Their Operator", uniqueEmail("theirop"), Role.MERCHANT, "Pass@123");
        Merchant myShop = merchants.register("My Shop", uniqueMerchantCode(), mine.getId(), Currency.RWF);
        merchants.register("Their Shop", uniqueMerchantCode(), theirs.getId(), Currency.RWF);

        Merchant resolved = merchants.findOwnShop(mine.getId()).orElseThrow(IllegalStateException::new);
        assertEquals(myShop.getId(), resolved.getId());
    }
}
