package rw.ac.auca.tapwallet.application;

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
import rw.ac.auca.tapwallet.infrastructure.persistence.UnitOfWork;

import java.math.BigDecimal;

/**
 * Shared fixture helpers. Every test uses unique emails and codes so the tests
 * stay independent even though they share one in-memory database.
 */
public abstract class ApplicationServiceTestSupport {

    private static final java.util.concurrent.atomic.AtomicInteger CODE_SEQUENCE =
            new java.util.concurrent.atomic.AtomicInteger();

    protected String uniqueEmail(String prefix) {
        return prefix + "-" + System.nanoTime() + "@example.com";
    }

    /**
     * A merchant code is only three letters and three digits, so a time-based
     * suffix collides. A counter guarantees every code in a test run is distinct.
     */
    protected String uniqueMerchantCode() {
        int next = CODE_SEQUENCE.incrementAndGet();
        char letter = (char) ('A' + ((next / 1000) % 26));
        return String.format("%cZZ-%03d", letter, next % 1000);
    }

    protected User createUser(String fullName, String email, Role role, String rawPassword) {
        return UnitOfWork.execute(() -> {
            User user = User.register(fullName, EmailAddress.of(email), PhoneNumber.of("0788123456"),
                    ServiceRegistry.passwordHasher().hash(rawPassword), role);
            return ServiceRegistry.userRepository().save(user);
        });
    }

    protected Wallet createWalletFor(User owner, String openingBalance) {
        return UnitOfWork.execute(() -> {
            User attached = ServiceRegistry.userRepository().findById(owner.getId()).orElseThrow(IllegalStateException::new);
            return ServiceRegistry.walletRepository()
                    .save(Wallet.openFor(attached, Money.of(new BigDecimal(openingBalance), Currency.RWF)));
        });
    }

    protected Merchant createMerchantFor(User operator, String businessName) {
        return UnitOfWork.execute(() -> {
            User attached = ServiceRegistry.userRepository().findById(operator.getId()).orElseThrow(IllegalStateException::new);
            return ServiceRegistry.merchantRepository().save(
                    Merchant.register(businessName, MerchantCode.of(uniqueMerchantCode()), attached, Money.zero(Currency.RWF)));
        });
    }

    protected Money balanceOf(Long walletId) {
        return UnitOfWork.execute(() -> ServiceRegistry.walletRepository().findById(walletId)
                .orElseThrow(IllegalStateException::new).getBalance());
    }

    protected Money revenueOf(Long merchantId) {
        return UnitOfWork.execute(() -> ServiceRegistry.merchantRepository().findById(merchantId)
                .orElseThrow(IllegalStateException::new).getRevenue());
    }
}
