package rw.ac.auca.tapwallet.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "wallet_transaction")
public class Transaction extends Audit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long id;

    @NotNull(message = "Sender wallet is required")
    @ManyToOne
    @JoinColumn(name = "sender_wallet_id", nullable = false)
    private Wallet senderWallet;

    @NotNull(message = "Receiving merchant is required")
    @ManyToOne
    @JoinColumn(name = "receiver_merchant_id", nullable = false)
    private Merchant receiverMerchant;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "status", nullable = false)
    private String status = "COMPLETED";

    public Transaction() {
    }

    public Transaction(Wallet senderWallet, Merchant receiverMerchant, BigDecimal amount, String status) {
        this.senderWallet = senderWallet;
        this.receiverMerchant = receiverMerchant;
        this.amount = amount;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Wallet getSenderWallet() {
        return senderWallet;
    }

    public void setSenderWallet(Wallet senderWallet) {
        this.senderWallet = senderWallet;
    }

    public Merchant getReceiverMerchant() {
        return receiverMerchant;
    }

    public void setReceiverMerchant(Merchant receiverMerchant) {
        this.receiverMerchant = receiverMerchant;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
