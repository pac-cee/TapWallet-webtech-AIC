package rw.ac.auca.tapwallet.application;

import rw.ac.auca.tapwallet.domain.exception.DomainException;
import rw.ac.auca.tapwallet.domain.model.shared.AccountStatus;
import rw.ac.auca.tapwallet.domain.model.shared.Currency;
import rw.ac.auca.tapwallet.domain.model.shared.Money;
import rw.ac.auca.tapwallet.domain.model.user.EmailAddress;
import rw.ac.auca.tapwallet.domain.model.user.PhoneNumber;
import rw.ac.auca.tapwallet.domain.model.user.Role;
import rw.ac.auca.tapwallet.domain.model.user.User;
import rw.ac.auca.tapwallet.domain.model.wallet.Wallet;
import rw.ac.auca.tapwallet.domain.repository.MerchantRepository;
import rw.ac.auca.tapwallet.domain.repository.PaymentRepository;
import rw.ac.auca.tapwallet.domain.repository.UserRepository;
import rw.ac.auca.tapwallet.domain.repository.WalletRepository;
import rw.ac.auca.tapwallet.infrastructure.persistence.UnitOfWork;
import rw.ac.auca.tapwallet.infrastructure.security.PasswordHasher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Administrator use cases for user accounts. A customer is always created with a
 * wallet in the same transaction, so an account can never exist half-built.
 */
public class UserApplicationService {

    private final UserRepository users;
    private final WalletRepository wallets;
    private final MerchantRepository merchants;
    private final PaymentRepository payments;
    private final PasswordHasher passwordHasher;

    public UserApplicationService(UserRepository users, WalletRepository wallets, MerchantRepository merchants,
                                  PaymentRepository payments, PasswordHasher passwordHasher) {
        this.users = users;
        this.wallets = wallets;
        this.merchants = merchants;
        this.payments = payments;
        this.passwordHasher = passwordHasher;
    }

    public User register(String fullName, String email, String phone, String rawPassword, Role role,
                         BigDecimal openingBalance, Currency currency) {
        requirePassword(rawPassword);
        EmailAddress emailAddress = EmailAddress.of(email);
        PhoneNumber phoneNumber = PhoneNumber.of(phone);

        return UnitOfWork.execute(() -> {
            if (users.findByEmail(emailAddress.getValue()).isPresent()) {
                throw new DomainException("That email is already registered.");
            }
            User user = User.register(fullName, emailAddress, phoneNumber, passwordHasher.hash(rawPassword), role);
            users.save(user);

            if (role == Role.CUSTOMER) {
                BigDecimal opening = openingBalance == null ? BigDecimal.ZERO : openingBalance;
                Currency walletCurrency = currency == null ? Currency.RWF : currency;
                wallets.save(Wallet.openFor(user, Money.of(opening, walletCurrency)));
            }
            return user;
        });
    }

    public void update(Long userId, String fullName, String email, String phone, String rawPassword, AccountStatus status) {
        EmailAddress emailAddress = EmailAddress.of(email);
        PhoneNumber phoneNumber = PhoneNumber.of(phone);

        UnitOfWork.run(() -> {
            User user = users.findById(userId)
                    .orElseThrow(() -> new DomainException("That user no longer exists."));

            Optional<User> sameEmail = users.findByEmail(emailAddress.getValue());
            if (sameEmail.isPresent() && !sameEmail.get().getId().equals(userId)) {
                throw new DomainException("That email is already registered to someone else.");
            }

            user.rename(fullName);
            user.changeEmail(emailAddress);
            user.changePhone(phoneNumber);
            if (status == AccountStatus.FROZEN) {
                user.freeze();
            } else {
                user.activate();
            }
            if (rawPassword != null && !rawPassword.trim().isEmpty()) {
                requirePassword(rawPassword);
                user.changePasswordHash(passwordHasher.hash(rawPassword));
            }
            users.save(user);
        });
    }

    public void delete(Long userId) {
        UnitOfWork.run(() -> {
            User user = users.findById(userId)
                    .orElseThrow(() -> new DomainException("That user no longer exists."));

            if (!payments.findByPayerId(userId).isEmpty() || !payments.findByMerchantOperatorId(userId).isEmpty()) {
                throw new DomainException("This user has payment history and cannot be deleted.");
            }
            if (merchants.findByOperatorId(userId).isPresent()) {
                throw new DomainException("This user still operates a shop. Delete the merchant first.");
            }

            wallets.findByOwnerId(userId).ifPresent(wallets::delete);
            users.delete(user);
        });
    }

    public List<User> findAll() {
        return UnitOfWork.execute(users::findAll);
    }

    public List<User> findByRole(Role role) {
        return UnitOfWork.execute(() -> users.findByRole(role));
    }

    public Optional<User> findById(Long id) {
        return UnitOfWork.execute(() -> users.findById(id));
    }

    private void requirePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.trim().length() < 6) {
            throw new DomainException("Password must be at least 6 characters.");
        }
    }
}
