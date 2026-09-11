package rw.ac.auca.tapwallet.domain.model.payment;

import rw.ac.auca.tapwallet.domain.exception.DomainException;
import rw.ac.auca.tapwallet.domain.model.merchant.Merchant;
import rw.ac.auca.tapwallet.domain.model.shared.AuditableEntity;
import rw.ac.auca.tapwallet.domain.model.shared.Money;
import rw.ac.auca.tapwallet.domain.model.wallet.Wallet;

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
import java.util.UUID;

/**
 * Aggregate root recording one customer-to-merchant payment. A payment is a
 * historical fact: it is written once and can only ever be reversed, never edited.
 */
@Entity
@Table(name = "payment")
public class Payment extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @Column(name = "reference", nullable = false, unique = true, length = 20)
    private String reference;

    @NotNull(message = "Paying wallet is required")
    @ManyToOne(optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @NotNull(message = "Receiving merchant is required")
    @ManyToOne(optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Valid
    @NotNull
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "amount", nullable = false, precision = 19, scale = 2)),
            @AttributeOverride(name = "currency", column = @Column(name = "currency", nullable = false, length = 3))
    })
    private Money amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private PaymentStatus status = PaymentStatus.COMPLETED;

    protected Payment() {
    }

    private Payment(Wallet wallet, Merchant merchant, Money amount) {
        if (wallet == null || merchant == null) {
            throw new DomainException("A payment needs both a wallet and a merchant.");
        }
        if (amount == null || !amount.isPositive()) {
            throw new DomainException("Payment amount must be greater than zero.");
        }
        this.wallet = wallet;
        this.merchant = merchant;
        this.amount = amount;
        this.status = PaymentStatus.COMPLETED;
        this.reference = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /** Factory method — records a completed payment. */
    public static Payment record(Wallet wallet, Merchant merchant, Money amount) {
        return new Payment(wallet, merchant, amount);
    }

    public void markReversed() {
        if (status == PaymentStatus.REVERSED) {
            throw new DomainException("This payment has already been reversed.");
        }
        this.status = PaymentStatus.REVERSED;
    }

    public boolean isReversed() {
        return status == PaymentStatus.REVERSED;
    }

    public boolean wasPaidBy(Long userId) {
        return wallet != null && wallet.isOwnedBy(userId);
    }

    public boolean wasReceivedBy(Long userId) {
        return merchant != null && merchant.isOperatedBy(userId);
    }

    public Long getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public Money getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }
}
