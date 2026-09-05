package rw.ac.auca.tapwallet;

import rw.ac.auca.tapwallet.dao.MerchantDao;
import rw.ac.auca.tapwallet.dao.UserDao;
import rw.ac.auca.tapwallet.dao.WalletDao;
import rw.ac.auca.tapwallet.model.Merchant;
import rw.ac.auca.tapwallet.model.User;
import rw.ac.auca.tapwallet.model.Wallet;
import rw.ac.auca.tapwallet.util.PasswordUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.math.BigDecimal;

public class DemoDataSeeder implements ServletContextListener {

    private final UserDao userDao = new UserDao();
    private final WalletDao walletDao = new WalletDao();
    private final MerchantDao merchantDao = new MerchantDao();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (!merchantDao.findAll().isEmpty()) {
            return;
        }

        registerCustomer("Eric Niyonzima", "eric.niyonzima@example.com", "0788123456", new BigDecimal("50000.00"));
        registerCustomer("Alice Uwase", "alice.uwase@example.com", "0722222222", new BigDecimal("20000.00"));

        User cafeOperator = registerCustomer("Jean Mugisha", "jean.mugisha@example.com", "0733333333", BigDecimal.ZERO);
        User shopOperator = registerCustomer("Grace Umutoni", "grace.umutoni@example.com", "0789999999", BigDecimal.ZERO);

        registerMerchant("AUCA Cafeteria", "CAF-001", cafeOperator);
        registerMerchant("Kigali Superette", "SUP-002", shopOperator);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

    private User registerCustomer(String fullName, String email, String phone, BigDecimal openingBalance) {
        User user = new User(fullName, email, phone, PasswordUtil.hash("Demo1234"), "ACTIVE");
        userDao.save(user);
        Wallet wallet = new Wallet(user, openingBalance, "RWF", "ACTIVE");
        walletDao.save(wallet);
        return user;
    }

    private void registerMerchant(String businessName, String merchantCode, User operator) {
        Merchant merchant = new Merchant(businessName, merchantCode, operator, "ACTIVE");
        merchantDao.save(merchant);
    }
}
