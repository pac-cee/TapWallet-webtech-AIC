package rw.ac.auca.tapwallet.dao;

import org.junit.Test;
import rw.ac.auca.tapwallet.model.User;
import rw.ac.auca.tapwallet.model.Wallet;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.*;

public class WalletDaoTest {

    private final UserDao userDao = new UserDao();
    private final WalletDao walletDao = new WalletDao();

    private User newOwner(String label) {
        String emailLocalPart = label.toLowerCase().replace(" ", "") + "-" + System.nanoTime();
        User owner = new User(label, emailLocalPart + "@example.com",
                "0788123456", "hash", "CUSTOMER", "ACTIVE");
        userDao.save(owner);
        return owner;
    }

    @Test
    public void savingAWalletAssignsAnId() {
        Wallet wallet = new Wallet(newOwner("Wallet Owner 1"), new BigDecimal("1000.00"), "RWF", "ACTIVE");
        walletDao.save(wallet);
        assertNotNull(wallet.getId());
    }

    @Test
    public void findByIdReturnsTheOwnerToo() {
        User owner = newOwner("Wallet Owner 2");
        Wallet wallet = new Wallet(owner, new BigDecimal("500.00"), "RWF", "ACTIVE");
        walletDao.save(wallet);

        Wallet found = walletDao.findById(wallet.getId());
        assertNotNull(found);
        assertEquals(owner.getId(), found.getOwner().getId());
        assertEquals(0, new BigDecimal("500.00").compareTo(found.getBalance()));
    }

    @Test
    public void savingWithAnExistingIdUpdatesTheBalance() {
        Wallet wallet = new Wallet(newOwner("Wallet Owner 3"), new BigDecimal("100.00"), "RWF", "ACTIVE");
        walletDao.save(wallet);

        wallet.setBalance(new BigDecimal("250.00"));
        walletDao.save(wallet);

        Wallet found = walletDao.findById(wallet.getId());
        assertEquals(0, new BigDecimal("250.00").compareTo(found.getBalance()));
    }

    @Test
    public void deleteRemovesTheWallet() {
        Wallet wallet = new Wallet(newOwner("Wallet Owner 4"), new BigDecimal("10.00"), "RWF", "ACTIVE");
        walletDao.save(wallet);
        Long id = wallet.getId();

        walletDao.delete(id);

        assertNull(walletDao.findById(id));
    }

    @Test
    public void findAllIncludesASavedWallet() {
        Wallet wallet = new Wallet(newOwner("Wallet Owner 5"), new BigDecimal("10.00"), "RWF", "ACTIVE");
        walletDao.save(wallet);

        List<Wallet> all = walletDao.findAll();
        assertTrue(all.stream().anyMatch(w -> w.getId().equals(wallet.getId())));
    }
}
