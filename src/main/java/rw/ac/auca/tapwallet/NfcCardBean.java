package rw.ac.auca.tapwallet;

import rw.ac.auca.tapwallet.dao.NfcCardDao;
import rw.ac.auca.tapwallet.dao.WalletDao;
import rw.ac.auca.tapwallet.model.NfcCard;
import rw.ac.auca.tapwallet.model.Wallet;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import java.util.List;
import java.util.UUID;

@ManagedBean
public class NfcCardBean {
    private NfcCardDao cardDao = new NfcCardDao();
    private WalletDao walletDao = new WalletDao();

    private Long id;
    private Long walletId;
    private String token;
    private String status = "ACTIVE";

    public String save() {
        if (walletId == null) {
            addError("Could not save card", "Please choose a wallet.");
            return null;
        }
        String cleanToken = token == null ? null : token.trim().toUpperCase();
        if (cleanToken == null || cleanToken.isEmpty()) {
            cleanToken = "TAP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        NfcCard existing = cardDao.findByToken(cleanToken);
        if (existing != null && (id == null || !existing.getId().equals(id))) {
            addError("Could not save card", "That card token is already linked.");
            return null;
        }

        Wallet wallet = walletDao.findById(walletId);
        if (wallet == null) {
            addError("Could not save card", "The chosen wallet no longer exists.");
            return null;
        }

        NfcCard card;
        if (id != null) {
            card = cardDao.findById(id);
            if (card == null) {
                return "card-list?faces-redirect=true";
            }
        } else {
            card = new NfcCard();
            if (cardDao.findByWallet(walletId) != null) {
                addError("Could not save card", "This wallet already has a card.");
                return null;
            }
        }

        card.setToken(cleanToken);
        card.setWallet(wallet);
        card.setStatus(status);

        try {
            cardDao.save(card);
        } catch (RuntimeException ex) {
            addError("Could not save card", "Please check the values and try again.");
            return null;
        }
        return "card-list?faces-redirect=true";
    }

    public void loadForEdit() {
        if (id != null) {
            NfcCard card = cardDao.findById(id);
            if (card != null) {
                walletId = card.getWallet().getId();
                token = card.getToken();
                status = card.getStatus();
            }
        }
    }

    public String delete(Long cardId) {
        try {
            cardDao.delete(cardId);
        } catch (RuntimeException ex) {
            addError("Could not delete card", "Please try again.");
            return null;
        }
        return "card-list?faces-redirect=true";
    }

    public List<NfcCard> getAllCards() {
        return cardDao.findAll();
    }

    public List<Wallet> getAllWalletsForDropdown() {
        return walletDao.findAll();
    }

    private void addError(String summary, String detail) {
        if (FacesContext.getCurrentInstance() != null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, detail));
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWalletId() {
        return walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
