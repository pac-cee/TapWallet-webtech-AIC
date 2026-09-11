package rw.ac.auca.tapwallet.infrastructure.bootstrap;

import rw.ac.auca.tapwallet.domain.model.merchant.Merchant;
import rw.ac.auca.tapwallet.domain.model.merchant.MerchantCode;
import rw.ac.auca.tapwallet.domain.model.shared.Currency;
import rw.ac.auca.tapwallet.domain.model.shared.Money;
import rw.ac.auca.tapwallet.domain.model.user.EmailAddress;
import rw.ac.auca.tapwallet.domain.model.user.PhoneNumber;
import rw.ac.auca.tapwallet.domain.model.user.Role;
import rw.ac.auca.tapwallet.domain.model.user.User;
import rw.ac.auca.tapwallet.domain.model.wallet.Wallet;
import rw.ac.auca.tapwallet.infrastructure.config.ServiceRegistry;
import rw.ac.auca.tapwallet.infrastructure.persistence.SessionFactoryProvider;
import rw.ac.auca.tapwallet.infrastructure.persistence.UnitOfWork;
import rw.ac.auca.tapwallet.infrastructure.security.PasswordHasher;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.math.BigDecimal;

/**
 * Seeds the in-memory database on first start. Without this there would be no
 * administrator to sign in as, since the database is rebuilt on every restart.
 */
public class DataSeeder implements ServletContextListener {

    private static final String DEMO_PASSWORD = "Pass@123";
    private static final String ADMIN_PASSWORD = "Admin@123";

    @Override
    public void contextInitialized(ServletContextEvent event) {
        UnitOfWork.run(() -> {
            if (ServiceRegistry.userRepository().count() > 0) {
                return;
            }
            PasswordHasher hasher = ServiceRegistry.passwordHasher();

            createUser("System Administrator", "admin@tapwallet.rw", "0788000001",
                    hasher.hash(ADMIN_PASSWORD), Role.ADMIN, null);

            createUser("Alice Uwase", "alice@tapwallet.rw", "0788000002",
                    hasher.hash(DEMO_PASSWORD), Role.CUSTOMER, new BigDecimal("50000.00"));
            createUser("Eric Niyonzima", "eric@tapwallet.rw", "0733000003",
                    hasher.hash(DEMO_PASSWORD), Role.CUSTOMER, new BigDecimal("20000.00"));

            User cafeOperator = createUser("Jean Mugisha", "cafe@tapwallet.rw", "0722000004",
                    hasher.hash(DEMO_PASSWORD), Role.MERCHANT, null);
            User shopOperator = createUser("Grace Umutoni", "shop@tapwallet.rw", "0799000005",
                    hasher.hash(DEMO_PASSWORD), Role.MERCHANT, null);

            createMerchant("AUCA Cafeteria", "CAF-001", cafeOperator);
            createMerchant("Kigali Superette", "SUP-002", shopOperator);
        });
    }

    private User createUser(String fullName, String email, String phone, String passwordHash,
                            Role role, BigDecimal openingBalance) {
        User user = User.register(fullName, EmailAddress.of(email), PhoneNumber.of(phone), passwordHash, role);
        ServiceRegistry.userRepository().save(user);
        if (role == Role.CUSTOMER) {
            BigDecimal opening = openingBalance == null ? BigDecimal.ZERO : openingBalance;
            ServiceRegistry.walletRepository().save(Wallet.openFor(user, Money.of(opening, Currency.RWF)));
        }
        return user;
    }

    private void createMerchant(String businessName, String code, User operator) {
        Merchant merchant = Merchant.register(businessName, MerchantCode.of(code), operator, Money.zero(Currency.RWF));
        ServiceRegistry.merchantRepository().save(merchant);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        SessionFactoryProvider.shutdown();
    }
}
