package rw.ac.auca.tapwallet.domain.model.wallet;

import rw.ac.auca.tapwallet.domain.exception.DomainException;
import rw.ac.auca.tapwallet.domain.exception.InactiveAccountException;
import rw.ac.auca.tapwallet.domain.exception.InsufficientFundsException;
import rw.ac.auca.tapwallet.domain.model.shared.AccountStatus;
import rw.ac.auca.tapwallet.domain.model.shared.AuditableEntity;
import rw.ac.auca.tapwallet.domain.model.shared.Currency;
import rw.ac.auca.tapwallet.domain.model.shared.Money;
import rw.ac.auca.tapwallet.domain.model.user.User;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Aggregate root holding a customer's balance. All balance changes go through
 * {@link #credit(Money)} / {@link #debit(Money)} so the invariants can never be bypassed.
 */
@Entity
@Table(name = "wallet")
public class Wallet extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_id")
    private Long id;

    @NotNull(message = "Owner is required")
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User owner;

    @Valid
    @NotNull
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "balance_amount", nullable = false, precision = 19, scale = 2)),
            @AttributeOverride(name = "currency", column = @Column(name = "balance_currency", nullable = false, length = 3))
    })
    private Money balance;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private AccountStatus status = AccountStatus.ACTIVE;

    protected Wallet() {
    }

    private Wallet(User owner, Money openingBalance) {
        if (owner == null) {
            throw new DomainException("A wallet must belong to a user.");
        }
        if (openingBalance == null || openingBalance.isNegative()) {
            throw new DomainException("Opening balance cannot be negative.");
        }
        this.owner = owner;
        this.balance = openingBalance;
        this.status = AccountStatus.ACTIVE;
    }

    /** Factory method — opens a wallet for a user. */
    public static Wallet openFor(User owner, Money openingBalance) {
        return new Wallet(owner, openingBalance);
    }

    public static Wallet openFor(User owner, Currency currency) {
        return new Wallet(owner, Money.zero(currency));
    }

    public void credit(Money amount) {
        requireOperable(amount);
        this.balance = balance.plus(amount);
    }

    public void debit(Money amount) {
        requireOperable(amount);
        if (balance.isLessThan(amount)) {
            throw new InsufficientFundsException(
                    "Balance " + balance + " is not enough to pay " + amount + ".");
        }
        this.balance = balance.minus(amount);
    }

    private void requireOperable(Money amount) {
        if (amount == null || !amount.isPositive()) {
            throw new DomainException("Amount must be greater than zero.");
        }
        if (!status.isActive()) {
            throw new InactiveAccountException("This wallet is frozen.");
        }
        if (!balance.hasSameCurrencyAs(amount)) {
            throw new DomainException("This wallet holds " + balance.getCurrency() + ", not " + amount.getCurrency() + ".");
        }
    }

    public void freeze() {
        this.status = AccountStatus.FROZEN;
    }

    public void activate() {
        this.status = AccountStatus.ACTIVE;
    }

    public boolean isOwnedBy(Long userId) {
        return owner != null && owner.getId() != null && owner.getId().equals(userId);
    }

    public Long getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    public Money getBalance() {
        return balance;
    }

    public AccountStatus getStatus() {
        return status;
    }
}
