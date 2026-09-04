package rw.ac.auca.tapwallet.dao;

import org.junit.Test;
import rw.ac.auca.tapwallet.model.NfcCard;
import rw.ac.auca.tapwallet.model.User;
import rw.ac.auca.tapwallet.model.Wallet;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.Assert.*;

public class NfcCardDaoTest {

    private final UserDao userDao = new UserDao();
    private final WalletDao walletDao = new WalletDao();
    private final NfcCardDao cardDao = new NfcCardDao();

    private Wallet newWallet(String label) {
        User owner = new User(label, label.toLowerCase().replace(" ", "") + "-" + System.nanoTime() + "@example.com",
                "0788123456", "hash", "CUSTOMER", "ACTIVE");
        userDao.save(owner);
        Wallet wallet = new Wallet(owner, new BigDecimal("100.00"), "RWF", "ACTIVE");
        walletDao.save(wallet);
        return wallet;
    }

    @Test
    public void savingACardAssignsAnId() {
        NfcCard card = new NfcCard("TAP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                newWallet("Card Owner 1"), "ACTIVE");
        cardDao.save(card);
        assertNotNull(card.getId());
    }

    @Test
    public void findByTokenReturnsWhatWasSaved() {
        String token = "TAP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        NfcCard card = new NfcCard(token, newWallet("Card Owner 2"), "ACTIVE");
        cardDao.save(card);

        NfcCard found = cardDao.findByToken(token);
        assertNotNull(found);
        assertEquals(card.getId(), found.getId());
    }

    @Test
    public void findByWalletReturnsTheLinkedCard() {
        Wallet wallet = newWallet("Card Owner 3");
        NfcCard card = new NfcCard("TAP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(), wallet, "ACTIVE");
        cardDao.save(card);

        NfcCard found = cardDao.findByWallet(wallet.getId());
        assertNotNull(found);
        assertEquals(card.getId(), found.getId());
    }

    @Test
    public void deleteRemovesTheCard() {
        NfcCard card = new NfcCard("TAP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                newWallet("Card Owner 4"), "ACTIVE");
        cardDao.save(card);

        cardDao.delete(card.getId());

        assertNull(cardDao.findById(card.getId()));
    }
}
