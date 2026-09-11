package rw.ac.auca.tapwallet.application;

import org.junit.Test;
import rw.ac.auca.tapwallet.domain.exception.DomainException;
import rw.ac.auca.tapwallet.domain.exception.InsufficientFundsException;
import rw.ac.auca.tapwallet.domain.model.merchant.Merchant;
import rw.ac.auca.tapwallet.domain.model.payment.Payment;
import rw.ac.auca.tapwallet.domain.model.shared.Currency;
import rw.ac.auca.tapwallet.domain.model.shared.Money;
import rw.ac.auca.tapwallet.domain.model.user.Role;
import rw.ac.auca.tapwallet.domain.model.user.User;
import rw.ac.auca.tapwallet.domain.model.wallet.Wallet;
import rw.ac.auca.tapwallet.infrastructure.config.ServiceRegistry;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.*;

public class PaymentApplicationServiceTest extends ApplicationServiceTestSupport {

    private final PaymentApplicationService payments = ServiceRegistry.payments();

    @Test
    public void payingMovesMoneyAndRecordsTheLedgerRowInOneTransaction() {
        User customer = createUser("Alice Uwase", uniqueEmail("payer"), Role.CUSTOMER, "Pass@123");
        Wallet wallet = createWalletFor(customer, "3000.00");
        User operator = createUser("Jean Mugisha", uniqueEmail("operator"), Role.MERCHANT, "Pass@123");
        Merchant shop = createMerchantFor(operator, "AUCA Cafeteria");

        Payment payment = payments.payMerchant(customer.getId(), shop.getId(), new BigDecimal("1200.00"));

        assertNotNull(payment.getId());
        assertEquals(Money.of("1800.00", Currency.RWF), balanceOf(wallet.getId()));
        assertEquals(Money.of("1200.00", Currency.RWF), revenueOf(shop.getId()));
    }

    @Test
    public void payingMoreThanTheBalanceRollsBackEverything() {
        User customer = createUser("Broke Customer", uniqueEmail("broke"), Role.CUSTOMER, "Pass@123");
        Wallet wallet = createWalletFor(customer, "100.00");
        User operator = createUser("Shop Owner", uniqueEmail("shopowner"), Role.MERCHANT, "Pass@123");
        Merchant shop = createMerchantFor(operator, "Kigali Superette");

        try {
            payments.payMerchant(customer.getId(), shop.getId(), new BigDecimal("9000.00"));
            fail("Expected an InsufficientFundsException");
        } catch (InsufficientFundsException expected) {
            assertNotNull(expected.getMessage());
        }

        assertEquals(Money.of("100.00", Currency.RWF), balanceOf(wallet.getId()));
        assertEquals(Money.zero(Currency.RWF), revenueOf(shop.getId()));
        assertTrue(payments.findOwnPayments(customer.getId()).isEmpty());
    }

    @Test
    public void aCustomerOnlySeesTheirOwnPayments() {
        User alice = createUser("Alice", uniqueEmail("alice"), Role.CUSTOMER, "Pass@123");
        createWalletFor(alice, "5000.00");
        User eric = createUser("Eric", uniqueEmail("eric"), Role.CUSTOMER, "Pass@123");
        createWalletFor(eric, "5000.00");
        User operator = createUser("Operator", uniqueEmail("op"), Role.MERCHANT, "Pass@123");
        Merchant shop = createMerchantFor(operator, "Corner Shop");

        payments.payMerchant(alice.getId(), shop.getId(), new BigDecimal("100.00"));
        payments.payMerchant(eric.getId(), shop.getId(), new BigDecimal("200.00"));

        List<Payment> aliceSees = payments.findOwnPayments(alice.getId());
        assertEquals(1, aliceSees.size());
        assertTrue(aliceSees.get(0).wasPaidBy(alice.getId()));
        assertFalse(aliceSees.get(0).wasPaidBy(eric.getId()));
    }

    @Test
    public void aMerchantOnlySeesPaymentsMadeToTheirOwnShop() {
        User customer = createUser("Paying Customer", uniqueEmail("customer"), Role.CUSTOMER, "Pass@123");
        createWalletFor(customer, "5000.00");

        User firstOperator = createUser("First Operator", uniqueEmail("op1"), Role.MERCHANT, "Pass@123");
        Merchant firstShop = createMerchantFor(firstOperator, "First Shop");
        User secondOperator = createUser("Second Operator", uniqueEmail("op2"), Role.MERCHANT, "Pass@123");
        createMerchantFor(secondOperator, "Second Shop");

        payments.payMerchant(customer.getId(), firstShop.getId(), new BigDecimal("300.00"));

        assertEquals(1, payments.findPaymentsReceived(firstOperator.getId()).size());
        assertTrue("the other merchant must not see it",
                payments.findPaymentsReceived(secondOperator.getId()).isEmpty());
    }

    @Test
    public void reversingAPaymentPutsTheMoneyBackOnBothSides() {
        User customer = createUser("Reversal Customer", uniqueEmail("reversal"), Role.CUSTOMER, "Pass@123");
        Wallet wallet = createWalletFor(customer, "2000.00");
        User operator = createUser("Reversal Operator", uniqueEmail("revop"), Role.MERCHANT, "Pass@123");
        Merchant shop = createMerchantFor(operator, "Reversal Shop");

        Payment payment = payments.payMerchant(customer.getId(), shop.getId(), new BigDecimal("500.00"));
        payments.reverse(payment.getId());

        assertEquals(Money.of("2000.00", Currency.RWF), balanceOf(wallet.getId()));
        assertEquals(Money.zero(Currency.RWF), revenueOf(shop.getId()));
    }

    @Test
    public void payingWithoutAWalletIsRefused() {
        User operator = createUser("No Wallet Operator", uniqueEmail("nwop"), Role.MERCHANT, "Pass@123");
        Merchant shop = createMerchantFor(operator, "No Wallet Shop");
        User walletless = createUser("No Wallet", uniqueEmail("nowallet"), Role.CUSTOMER, "Pass@123");

        try {
            payments.payMerchant(walletless.getId(), shop.getId(), new BigDecimal("10.00"));
            fail("Expected a DomainException");
        } catch (DomainException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void zeroOrNegativeAmountsAreRefused() {
        User customer = createUser("Zero Customer", uniqueEmail("zero"), Role.CUSTOMER, "Pass@123");
        createWalletFor(customer, "1000.00");
        User operator = createUser("Zero Operator", uniqueEmail("zeroop"), Role.MERCHANT, "Pass@123");
        Merchant shop = createMerchantFor(operator, "Zero Shop");

        try {
            payments.payMerchant(customer.getId(), shop.getId(), BigDecimal.ZERO);
            fail("Expected a DomainException");
        } catch (DomainException expected) {
            assertNotNull(expected.getMessage());
        }
    }
}
