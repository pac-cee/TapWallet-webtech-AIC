package rw.ac.auca.tapwallet.domain;

import org.junit.Before;
import org.junit.Test;
import rw.ac.auca.tapwallet.domain.exception.InactiveAccountException;
import rw.ac.auca.tapwallet.domain.exception.InsufficientFundsException;
import rw.ac.auca.tapwallet.domain.exception.SelfPaymentException;
import rw.ac.auca.tapwallet.domain.model.merchant.Merchant;
import rw.ac.auca.tapwallet.domain.model.merchant.MerchantCode;
import rw.ac.auca.tapwallet.domain.model.payment.Payment;
import rw.ac.auca.tapwallet.domain.model.shared.Currency;
import rw.ac.auca.tapwallet.domain.model.shared.Money;
import rw.ac.auca.tapwallet.domain.model.user.EmailAddress;
import rw.ac.auca.tapwallet.domain.model.user.PhoneNumber;
import rw.ac.auca.tapwallet.domain.model.user.Role;
import rw.ac.auca.tapwallet.domain.model.user.User;
import rw.ac.auca.tapwallet.domain.model.wallet.Wallet;
import rw.ac.auca.tapwallet.domain.service.PaymentDomainService;

import static org.junit.Assert.*;

public class PaymentDomainServiceTest {

    private final PaymentDomainService paymentDomainService = new PaymentDomainService();

    private Wallet customerWallet;
    private Merchant shop;

    @Before
    public void setUp() {
        User customer = User.register("Alice Uwase", EmailAddress.of("alice@example.com"),
                PhoneNumber.of("0788123456"), "hash", Role.CUSTOMER);
        User operator = User.register("Jean Mugisha", EmailAddress.of("jean@example.com"),
                PhoneNumber.of("0722123456"), "hash", Role.MERCHANT);

        customerWallet = Wallet.openFor(customer, Money.of("2000.00", Currency.RWF));
        shop = Merchant.register("AUCA Cafeteria", MerchantCode.of("CAF-001"), operator, Money.zero(Currency.RWF));
    }

    @Test
    public void payingMovesMoneyFromTheWalletToTheMerchant() {
        Payment payment = paymentDomainService.pay(customerWallet, shop, Money.of("750.00", Currency.RWF));

        assertEquals(Money.of("1250.00", Currency.RWF), customerWallet.getBalance());
        assertEquals(Money.of("750.00", Currency.RWF), shop.getRevenue());
        assertEquals(Money.of("750.00", Currency.RWF), payment.getAmount());
        assertFalse(payment.isReversed());
        assertNotNull(payment.getReference());
    }

    @Test
    public void payingMoreThanTheBalanceLeavesBothSidesUntouched() {
        try {
            paymentDomainService.pay(customerWallet, shop, Money.of("9000.00", Currency.RWF));
            fail("Expected an InsufficientFundsException");
        } catch (InsufficientFundsException expected) {
            assertNotNull(expected.getMessage());
        }
        assertEquals(Money.of("2000.00", Currency.RWF), customerWallet.getBalance());
        assertEquals(Money.zero(Currency.RWF), shop.getRevenue());
    }

    @Test
    public void aMerchantCannotPayTheirOwnShop() {
        Wallet operatorWallet = Wallet.openFor(shop.getOperator(), Money.of("500.00", Currency.RWF));
        try {
            paymentDomainService.pay(operatorWallet, shop, Money.of("100.00", Currency.RWF));
            fail("Expected a SelfPaymentException");
        } catch (SelfPaymentException expected) {
            assertNotNull(expected.getMessage());
        }
        assertEquals(Money.of("500.00", Currency.RWF), operatorWallet.getBalance());
    }

    @Test
    public void aFrozenMerchantCannotBePaid() {
        shop.freeze();
        try {
            paymentDomainService.pay(customerWallet, shop, Money.of("100.00", Currency.RWF));
            fail("Expected an InactiveAccountException");
        } catch (InactiveAccountException expected) {
            assertNotNull(expected.getMessage());
        }
        assertEquals(Money.of("2000.00", Currency.RWF), customerWallet.getBalance());
    }

    @Test
    public void reversingAPaymentPutsTheMoneyBack() {
        Payment payment = paymentDomainService.pay(customerWallet, shop, Money.of("600.00", Currency.RWF));

        paymentDomainService.reverse(payment);

        assertEquals(Money.of("2000.00", Currency.RWF), customerWallet.getBalance());
        assertEquals(Money.zero(Currency.RWF), shop.getRevenue());
        assertTrue(payment.isReversed());
    }

    @Test
    public void aPaymentCannotBeReversedTwice() {
        Payment payment = paymentDomainService.pay(customerWallet, shop, Money.of("600.00", Currency.RWF));
        paymentDomainService.reverse(payment);
        try {
            paymentDomainService.reverse(payment);
            fail("Expected a DomainException");
        } catch (RuntimeException expected) {
            assertNotNull(expected.getMessage());
        }
    }
}
