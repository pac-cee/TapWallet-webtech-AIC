package rw.ac.auca.tapwallet.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Entity
@Table(name = "nfc_card")
public class NfcCard extends Audit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_id")
    private Long id;

    @NotBlank(message = "Card token is required")
    @Size(min = 8, max = 32, message = "Card token must be between 8 and 32 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Card token may only contain A-Z, 0-9 and dashes")
    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @NotNull(message = "Wallet is required")
    @ManyToOne
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    public NfcCard() {
    }

    public NfcCard(String token, Wallet wallet, String status) {
        this.token = token;
        this.wallet = wallet;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
