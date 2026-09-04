package rw.ac.auca.tapwallet.dao;

import org.junit.Test;
import rw.ac.auca.tapwallet.model.Merchant;
import rw.ac.auca.tapwallet.model.User;

import static org.junit.Assert.*;

public class MerchantDaoTest {

    private final UserDao userDao = new UserDao();
    private final MerchantDao merchantDao = new MerchantDao();

    private User newOperator(String label) {
        User operator = new User(label, label.toLowerCase().replace(" ", "") + "-" + System.nanoTime() + "@example.com",
                "0788123456", "hash", "MERCHANT", "ACTIVE");
        userDao.save(operator);
        return operator;
    }

    @Test
    public void savingAMerchantAssignsAnId() {
        Merchant merchant = new Merchant("Shop " + System.nanoTime(), "MCH-" + System.nanoTime() % 100000,
                newOperator("Merchant Op 1"), "ACTIVE");
        merchantDao.save(merchant);
        assertNotNull(merchant.getId());
    }

    @Test
    public void findByCodeReturnsWhatWasSaved() {
        String code = "MCH-" + Math.abs(System.nanoTime() % 1000000);
        Merchant merchant = new Merchant("Code Shop", code, newOperator("Merchant Op 2"), "ACTIVE");
        merchantDao.save(merchant);

        Merchant found = merchantDao.findByCode(code);
        assertNotNull(found);
        assertEquals(merchant.getId(), found.getId());
    }

    @Test
    public void findByOperatorReturnsTheShop() {
        User operator = newOperator("Merchant Op 3");
        Merchant merchant = new Merchant("Op Shop", "MCH-" + Math.abs(System.nanoTime() % 1000000), operator, "ACTIVE");
        merchantDao.save(merchant);

        Merchant found = merchantDao.findByOperator(operator.getId());
        assertNotNull(found);
        assertEquals(merchant.getId(), found.getId());
    }

    @Test
    public void deleteRemovesTheMerchant() {
        Merchant merchant = new Merchant("Gone Shop", "MCH-" + Math.abs(System.nanoTime() % 1000000),
                newOperator("Merchant Op 4"), "ACTIVE");
        merchantDao.save(merchant);

        merchantDao.delete(merchant.getId());

        assertNull(merchantDao.findById(merchant.getId()));
    }
}
