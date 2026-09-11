package rw.ac.auca.tapwallet.infrastructure.config;

import rw.ac.auca.tapwallet.application.AuthenticationService;
import rw.ac.auca.tapwallet.application.MerchantApplicationService;
import rw.ac.auca.tapwallet.application.PaymentApplicationService;
import rw.ac.auca.tapwallet.application.UserApplicationService;
import rw.ac.auca.tapwallet.application.WalletApplicationService;
import rw.ac.auca.tapwallet.domain.repository.MerchantRepository;
import rw.ac.auca.tapwallet.domain.repository.PaymentRepository;
import rw.ac.auca.tapwallet.domain.repository.UserRepository;
import rw.ac.auca.tapwallet.domain.repository.WalletRepository;
import rw.ac.auca.tapwallet.domain.service.PaymentDomainService;
import rw.ac.auca.tapwallet.infrastructure.persistence.HibernateMerchantRepository;
import rw.ac.auca.tapwallet.infrastructure.persistence.HibernatePaymentRepository;
import rw.ac.auca.tapwallet.infrastructure.persistence.HibernateUserRepository;
import rw.ac.auca.tapwallet.infrastructure.persistence.HibernateWalletRepository;
import rw.ac.auca.tapwallet.infrastructure.security.PasswordHasher;
import rw.ac.auca.tapwallet.infrastructure.security.Sha256PasswordHasher;

/**
 * Factory / composition root. This is the single place where the interfaces the
 * domain declares are bound to their Hibernate implementations, which keeps the
 * wiring out of the JSF beans (they only ever ask this registry for a service).
 */
public final class ServiceRegistry {

    private static final UserRepository USER_REPOSITORY = new HibernateUserRepository();
    private static final WalletRepository WALLET_REPOSITORY = new HibernateWalletRepository();
    private static final MerchantRepository MERCHANT_REPOSITORY = new HibernateMerchantRepository();
    private static final PaymentRepository PAYMENT_REPOSITORY = new HibernatePaymentRepository();

    private static final PasswordHasher PASSWORD_HASHER = new Sha256PasswordHasher();
    private static final PaymentDomainService PAYMENT_DOMAIN_SERVICE = new PaymentDomainService();

    private static final AuthenticationService AUTHENTICATION_SERVICE =
            new AuthenticationService(USER_REPOSITORY, PASSWORD_HASHER);
    private static final UserApplicationService USER_SERVICE =
            new UserApplicationService(USER_REPOSITORY, WALLET_REPOSITORY, MERCHANT_REPOSITORY, PAYMENT_REPOSITORY, PASSWORD_HASHER);
    private static final MerchantApplicationService MERCHANT_SERVICE =
            new MerchantApplicationService(MERCHANT_REPOSITORY, USER_REPOSITORY, PAYMENT_REPOSITORY);
    private static final WalletApplicationService WALLET_SERVICE =
            new WalletApplicationService(WALLET_REPOSITORY);
    private static final PaymentApplicationService PAYMENT_SERVICE =
            new PaymentApplicationService(PAYMENT_REPOSITORY, WALLET_REPOSITORY, MERCHANT_REPOSITORY, PAYMENT_DOMAIN_SERVICE);

    private ServiceRegistry() {
    }

    public static AuthenticationService authentication() {
        return AUTHENTICATION_SERVICE;
    }

    public static UserApplicationService users() {
        return USER_SERVICE;
    }

    public static MerchantApplicationService merchants() {
        return MERCHANT_SERVICE;
    }

    public static WalletApplicationService wallets() {
        return WALLET_SERVICE;
    }

    public static PaymentApplicationService payments() {
        return PAYMENT_SERVICE;
    }

    public static PasswordHasher passwordHasher() {
        return PASSWORD_HASHER;
    }

    public static UserRepository userRepository() {
        return USER_REPOSITORY;
    }

    public static WalletRepository walletRepository() {
        return WALLET_REPOSITORY;
    }

    public static MerchantRepository merchantRepository() {
        return MERCHANT_REPOSITORY;
    }

    public static PaymentRepository paymentRepository() {
        return PAYMENT_REPOSITORY;
    }
}
