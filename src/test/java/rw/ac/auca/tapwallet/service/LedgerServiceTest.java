package rw.ac.auca.tapwallet.service;

import org.junit.Test;
import rw.ac.auca.tapwallet.dao.MerchantDao;
import rw.ac.auca.tapwallet.dao.UserDao;
import rw.ac.auca.tapwallet.dao.WalletDao;
import rw.ac.auca.tapwallet.model.Merchant;
import rw.ac.auca.tapwallet.model.TopUp;
import rw.ac.auca.tapwallet.model.Transaction;
import rw.ac.auca.tapwallet.model.User;
import rw.ac.auca.tapwallet.model.Wallet;
import rw.ac.auca.tapwallet.model.Withdrawal;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class LedgerServiceTest {

    private final UserDao userDao = new UserDao();
    private final WalletDao walletDao = new WalletDao();
    private final MerchantDao merchantDao = new MerchantDao();
    private final PaymentService paymentService = new PaymentService();
    private final TopUpService topUpService = new TopUpService();
    private final WithdrawalService withdrawalService = new WithdrawalService();

    private User newUser(String label) {
        User user = new User(label, label.toLowerCase().replace(" ", "") + "-" + System.nanoTime() + "@example.com",
                "0788123456", "hash", "ACTIVE");
        userDao.save(user);
        return user;
    }

    private Wallet newWallet(String label, String amount) {
        Wallet wallet = new Wallet(newUser(label), new BigDecimal(amount), "RWF", "ACTIVE");
        walletDao.save(wallet);
        return wallet;
    }

    private Merchant newMerchant(String label) {
        Merchant merchant = new Merchant(label, "MCH-" + Math.abs(System.nanoTime() % 1000000), newUser(label + " Operator"), "ACTIVE");
        merchantDao.save(merchant);
        return merchant;
    }

    @Test
    public void topUpCreditsTheWallet() {
        Wallet wallet = newWallet("TopUp Owner", "100.00");

        topUpService.credit(new TopUp(wallet, new BigDecimal("50.00"), "MOMO"));

        Wallet reloaded = walletDao.findById(wallet.getId());
        assertEquals(0, new BigDecimal("150.00").compareTo(reloaded.getBalance()));
    }

    @Test
    public void withdrawalDebitsTheWallet() {
        Wallet wallet = newWallet("Withdrawal Owner", "100.00");

        withdrawalService.debit(new Withdrawal(wallet, new BigDecimal("40.00"), "MOMO"));

        Wallet reloaded = walletDao.findById(wallet.getId());
        assertEquals(0, new BigDecimal("60.00").compareTo(reloaded.getBalance()));
    }

    @Test
    public void withdrawalBeyondBalanceIsRejected() {
        Wallet wallet = newWallet("Poor Owner", "10.00");
        try {
            withdrawalService.debit(new Withdrawal(wallet, new BigDecimal("999.00"), "MOMO"));
            fail("Expected an IllegalStateException");
        } catch (IllegalStateException ex) {
            assertNotNull(ex.getMessage());
        }
        Wallet reloaded = walletDao.findById(wallet.getId());
        assertEquals(0, new BigDecimal("10.00").compareTo(reloaded.getBalance()));
    }

    @Test
    public void paymentMovesMoneyToTheMerchantAtomically() {
        Wallet sender = newWallet("Sender Owner", "200.00");
        Merchant merchant = newMerchant("Sender's Shop");

        paymentService.pay(new Transaction(sender, merchant, new BigDecimal("70.00"), "COMPLETED"));

        assertEquals(0, new BigDecimal("130.00").compareTo(walletDao.findById(sender.getId()).getBalance()));
        assertEquals(0, new BigDecimal("70.00").compareTo(merchantDao.findById(merchant.getId()).getBalance()));
    }

    @Test
    public void paymentBeyondBalanceLeavesBothBalancesUntouched() {
        Wallet sender = newWallet("Broke Sender", "20.00");
        Merchant merchant = newMerchant("Rich Shop");
        try {
            paymentService.pay(new Transaction(sender, merchant, new BigDecimal("500.00"), "COMPLETED"));
            fail("Expected an IllegalStateException");
        } catch (IllegalStateException ex) {
            assertNotNull(ex.getMessage());
        }
        assertEquals(0, new BigDecimal("20.00").compareTo(walletDao.findById(sender.getId()).getBalance()));
        assertEquals(0, BigDecimal.ZERO.compareTo(merchantDao.findById(merchant.getId()).getBalance()));
    }

    @Test
    public void aMerchantCannotPayItsOwnShop() {
        Merchant merchant = newMerchant("Self Pay Shop");
        Wallet operatorWallet = new Wallet(merchant.getOperator(), new BigDecimal("100.00"), "RWF", "ACTIVE");
        walletDao.save(operatorWallet);

        try {
            paymentService.pay(new Transaction(operatorWallet, merchant, new BigDecimal("10.00"), "COMPLETED"));
            fail("Expected an IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertNotNull(ex.getMessage());
        }
        assertEquals(0, new BigDecimal("100.00").compareTo(walletDao.findById(operatorWallet.getId()).getBalance()));
    }

    @Test
    public void reversingAPaymentMovesMoneyBack() {
        Wallet sender = newWallet("Reverse Sender", "200.00");
        Merchant merchant = newMerchant("Reverse Shop");
        Transaction tx = paymentService.pay(
                new Transaction(sender, merchant, new BigDecimal("60.00"), "COMPLETED"));

        paymentService.reverse(tx.getId());

        assertEquals(0, new BigDecimal("200.00").compareTo(walletDao.findById(sender.getId()).getBalance()));
        assertEquals(0, BigDecimal.ZERO.compareTo(merchantDao.findById(merchant.getId()).getBalance()));
        assertNull(paymentService.findById(tx.getId()));
    }
}
