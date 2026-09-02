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

/**
 * The Class Wallet.
 *
 * @author Pacifique Bakundukize
 * @version 1.0
 */
@Entity
@Table(name = "wallet")
public class Wallet extends Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_id")
    private Long id;

    @NotNull(message = "Owner is required")
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.0", message = "Balance cannot be negative")
    @Column(name = "balance", nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "currency", nullable = false)
    private String currency = "RWF";

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    public Wallet() {
    }

    public Wallet(User owner, BigDecimal balance, String currency, String status) {
        this.owner = owner;
        this.balance = balance;
        this.currency = currency;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
