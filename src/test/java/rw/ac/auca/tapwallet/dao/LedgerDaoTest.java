package rw.ac.auca.tapwallet.dao;

import org.junit.Test;
import rw.ac.auca.tapwallet.model.TopUp;
import rw.ac.auca.tapwallet.model.Transaction;
import rw.ac.auca.tapwallet.model.User;
import rw.ac.auca.tapwallet.model.Wallet;
import rw.ac.auca.tapwallet.model.Withdrawal;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class LedgerDaoTest {

    private final UserDao userDao = new UserDao();
    private final WalletDao walletDao = new WalletDao();
    private final TransactionDao transactionDao = new TransactionDao();
    private final TopUpDao topUpDao = new TopUpDao();
    private final WithdrawalDao withdrawalDao = new WithdrawalDao();

    private Wallet newWallet(String label, String amount) {
        User owner = new User(label, label.toLowerCase().replace(" ", "") + "-" + System.nanoTime() + "@example.com",
                "0788123456", "hash", "CUSTOMER", "ACTIVE");
        userDao.save(owner);
        Wallet wallet = new Wallet(owner, new BigDecimal(amount), "RWF", "ACTIVE");
        walletDao.save(wallet);
        return wallet;
    }

    @Test
    public void topUpCreditsTheWallet() {
        Wallet wallet = newWallet("TopUp Owner", "100.00");

        topUpDao.credit(new TopUp(wallet, new BigDecimal("50.00"), "MOMO"));

        Wallet reloaded = walletDao.findById(wallet.getId());
        assertEquals(0, new BigDecimal("150.00").compareTo(reloaded.getBalance()));
    }

    @Test
    public void withdrawalDebitsTheWallet() {
        Wallet wallet = newWallet("Withdrawal Owner", "100.00");

        withdrawalDao.debit(new Withdrawal(wallet, new BigDecimal("40.00"), "MOMO"));

        Wallet reloaded = walletDao.findById(wallet.getId());
        assertEquals(0, new BigDecimal("60.00").compareTo(reloaded.getBalance()));
    }

    @Test
    public void withdrawalBeyondBalanceIsRejected() {
        Wallet wallet = newWallet("Poor Owner", "10.00");
        try {
            withdrawalDao.debit(new Withdrawal(wallet, new BigDecimal("999.00"), "MOMO"));
            fail("Expected an IllegalStateException");
        } catch (IllegalStateException ex) {
            assertNotNull(ex.getMessage());
        }
        Wallet reloaded = walletDao.findById(wallet.getId());
        assertEquals(0, new BigDecimal("10.00").compareTo(reloaded.getBalance()));
    }

    @Test
    public void transferMovesMoneyAtomically() {
        Wallet sender = newWallet("Sender Owner", "200.00");
        Wallet receiver = newWallet("Receiver Owner", "50.00");

        transactionDao.transfer(new Transaction(sender, receiver, new BigDecimal("70.00"), "PAYMENT", "COMPLETED"));

        assertEquals(0, new BigDecimal("130.00").compareTo(walletDao.findById(sender.getId()).getBalance()));
        assertEquals(0, new BigDecimal("120.00").compareTo(walletDao.findById(receiver.getId()).getBalance()));
    }

    @Test
    public void transferBeyondBalanceLeavesBothBalancesUntouched() {
        Wallet sender = newWallet("Broke Sender", "20.00");
        Wallet receiver = newWallet("Rich Receiver", "20.00");
        try {
            transactionDao.transfer(new Transaction(sender, receiver, new BigDecimal("500.00"), "PAYMENT", "COMPLETED"));
            fail("Expected an IllegalStateException");
        } catch (IllegalStateException ex) {
            assertNotNull(ex.getMessage());
        }
        assertEquals(0, new BigDecimal("20.00").compareTo(walletDao.findById(sender.getId()).getBalance()));
        assertEquals(0, new BigDecimal("20.00").compareTo(walletDao.findById(receiver.getId()).getBalance()));
    }

    @Test
    public void reversingATransferMovesMoneyBack() {
        Wallet sender = newWallet("Reverse Sender", "200.00");
        Wallet receiver = newWallet("Reverse Receiver", "50.00");
        Transaction tx = transactionDao.transfer(
                new Transaction(sender, receiver, new BigDecimal("60.00"), "PAYMENT", "COMPLETED"));

        transactionDao.deleteWithReversal(tx.getId());

        assertEquals(0, new BigDecimal("200.00").compareTo(walletDao.findById(sender.getId()).getBalance()));
        assertEquals(0, new BigDecimal("50.00").compareTo(walletDao.findById(receiver.getId()).getBalance()));
        assertNull(transactionDao.findById(tx.getId()));
    }
}
