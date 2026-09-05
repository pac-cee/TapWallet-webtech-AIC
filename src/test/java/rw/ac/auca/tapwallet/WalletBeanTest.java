package rw.ac.auca.tapwallet;

import org.junit.Test;
import rw.ac.auca.tapwallet.dao.UserDao;
import rw.ac.auca.tapwallet.dao.WalletDao;
import rw.ac.auca.tapwallet.model.User;
import rw.ac.auca.tapwallet.model.Wallet;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class WalletBeanTest {

    private final UserDao userDao = new UserDao();
    private final WalletDao walletDao = new WalletDao();

    private User newOwner(String label) {
        String emailLocalPart = label.toLowerCase().replace(" ", "") + "-" + System.nanoTime();
        User owner = new User(label, emailLocalPart + "@example.com",
                "0788123456", "hash", "ACTIVE");
        userDao.save(owner);
        return owner;
    }

    @Test
    public void savingANewWalletLinksItToTheChosenOwner() {
        User owner = newOwner("Bean Owner 1");

        WalletBean bean = new WalletBean();
        bean.setOwnerId(owner.getId());
        bean.setBalance(new BigDecimal("2000.00"));
        bean.setCurrency("RWF");
        bean.setStatus("ACTIVE");

        String outcome = bean.save();

        assertEquals("wallet-list?faces-redirect=true", outcome);
        Wallet saved = walletDao.findAll().stream()
                .filter(w -> w.getOwner().getId().equals(owner.getId()))
                .findFirst()
                .orElse(null);
        assertNotNull(saved);
        assertEquals(0, new BigDecimal("2000.00").compareTo(saved.getBalance()));
    }

    @Test
    public void editingAWalletUpdatesTheBalance() {
        User owner = newOwner("Bean Owner 2");
        Wallet wallet = new Wallet(owner, new BigDecimal("100.00"), "RWF", "ACTIVE");
        walletDao.save(wallet);

        WalletBean bean = new WalletBean();
        bean.setId(wallet.getId());
        bean.loadForEdit();
        bean.setBalance(new BigDecimal("300.00"));

        bean.save();

        Wallet reloaded = walletDao.findById(wallet.getId());
        assertEquals(0, new BigDecimal("300.00").compareTo(reloaded.getBalance()));
    }

    @Test
    public void deleteRemovesTheWallet() {
        Wallet wallet = new Wallet(newOwner("Bean Owner 3"), new BigDecimal("10.00"), "RWF", "ACTIVE");
        walletDao.save(wallet);

        WalletBean bean = new WalletBean();
        String outcome = bean.delete(wallet.getId());

        assertEquals("wallet-list?faces-redirect=true", outcome);
        assertNull(walletDao.findById(wallet.getId()));
    }
}
