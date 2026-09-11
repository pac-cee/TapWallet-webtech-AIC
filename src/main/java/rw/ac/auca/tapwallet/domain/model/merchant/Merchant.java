package rw.ac.auca.tapwallet.domain.model.merchant;

import rw.ac.auca.tapwallet.domain.exception.DomainException;
import rw.ac.auca.tapwallet.domain.exception.InactiveAccountException;
import rw.ac.auca.tapwallet.domain.model.shared.AccountStatus;
import rw.ac.auca.tapwallet.domain.model.shared.AuditableEntity;
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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Aggregate root for a shop that accepts payments. Its revenue only ever moves
 * through {@link #receive(Money)} / {@link #refund(Money)}.
 */
@Entity
@Table(name = "merchant")
public class Merchant extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "merchant_id")
    private Long id;

    @NotBlank(message = "Business name is required")
    @Size(min = 3, max = 80, message = "Business name must be between 3 and 80 characters")
    @Column(name = "business_name", nullable = false, length = 80)
    private String businessName;

    @Valid
    @NotNull
    @Embedded
    private MerchantCode code;

    @NotNull(message = "Operator is required")
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User operator;

    @Valid
    @NotNull
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "revenue_amount", nullable = false, precision = 19, scale = 2)),
            @AttributeOverride(name = "currency", column = @Column(name = "revenue_currency", nullable = false, length = 3))
    })
    private Money revenue;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private AccountStatus status = AccountStatus.ACTIVE;

    protected Merchant() {
    }

    private Merchant(String businessName, MerchantCode code, User operator, Money openingRevenue) {
        if (code == null) {
            throw new DomainException("A merchant code is required.");
        }
        if (operator == null) {
            throw new DomainException("A merchant must have an operator.");
        }
        if (openingRevenue == null || openingRevenue.isNegative()) {
            throw new DomainException("Revenue cannot start negative.");
        }
        rename(businessName);
        this.code = code;
        this.operator = operator;
        this.revenue = openingRevenue;
        this.status = AccountStatus.ACTIVE;
    }

    /** Factory method — registers a shop for its operating user. */
    public static Merchant register(String businessName, MerchantCode code, User operator, Money openingRevenue) {
        return new Merchant(businessName, code, operator, openingRevenue);
    }

    public void rename(String newBusinessName) {
        String trimmed = newBusinessName == null ? "" : newBusinessName.trim();
        if (trimmed.length() < 3) {
            throw new DomainException("Business name must be at least 3 characters.");
        }
        this.businessName = trimmed;
    }

    public void changeCode(MerchantCode newCode) {
        if (newCode == null) {
            throw new DomainException("A merchant code is required.");
        }
        this.code = newCode;
    }

    public void receive(Money amount) {
        if (amount == null || !amount.isPositive()) {
            throw new DomainException("Amount must be greater than zero.");
        }
        if (!status.isActive()) {
            throw new InactiveAccountException("This merchant is not accepting payments.");
        }
        this.revenue = revenue.plus(amount);
    }

    public void refund(Money amount) {
        if (amount == null || !amount.isPositive()) {
            throw new DomainException("Amount must be greater than zero.");
        }
        if (revenue.isLessThan(amount)) {
            throw new DomainException("Recorded revenue " + revenue + " is not enough to refund " + amount + ".");
        }
        this.revenue = revenue.minus(amount);
    }

    public void freeze() {
        this.status = AccountStatus.FROZEN;
    }

    public void activate() {
        this.status = AccountStatus.ACTIVE;
    }

    public boolean isOperatedBy(Long userId) {
        return operator != null && operator.getId() != null && operator.getId().equals(userId);
    }

    public boolean isActive() {
        return status.isActive();
    }

    public Long getId() {
        return id;
    }

    public String getBusinessName() {
        return businessName;
    }

    public MerchantCode getCode() {
        return code;
    }

    public User getOperator() {
        return operator;
    }

    public Money getRevenue() {
        return revenue;
    }

    public AccountStatus getStatus() {
        return status;
    }
}
