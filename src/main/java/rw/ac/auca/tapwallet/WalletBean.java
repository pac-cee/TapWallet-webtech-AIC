package rw.ac.auca.tapwallet;

import rw.ac.auca.tapwallet.dao.UserDao;
import rw.ac.auca.tapwallet.dao.WalletDao;
import rw.ac.auca.tapwallet.model.User;
import rw.ac.auca.tapwallet.model.Wallet;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@ManagedBean
public class WalletBean {
    private WalletDao walletDao = new WalletDao();
    private UserDao userDao = new UserDao();

    private Long id;
    private Long ownerId;
    private BigDecimal balance = BigDecimal.ZERO;
    private String currency = "RWF";
    private String status = "ACTIVE";

    public static final List<String> CURRENCIES = Arrays.asList("RWF", "USD", "EUR");

    public String save() {
        if (ownerId == null) {
            addError("Could not save wallet", "Please choose an owner.");
            return null;
        }
        if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0) {
            addError("Could not save wallet", "Balance cannot be negative.");
            return null;
        }
        String cleanCurrency = currency == null ? "RWF" : currency.trim().toUpperCase();
        if (!CURRENCIES.contains(cleanCurrency)) {
            addError("Could not save wallet", "Currency must be RWF, USD or EUR.");
            return null;
        }

        User owner = userDao.findById(ownerId);
        if (owner == null) {
            addError("Could not save wallet", "The chosen owner no longer exists.");
            return null;
        }

        Wallet wallet;
        boolean creating = (id == null);
        if (!creating) {
            wallet = walletDao.findById(id);
            if (wallet == null) {
                return "wallet-list?faces-redirect=true";
            }
        } else {
            wallet = new Wallet();

            if (walletDao.findByOwner(ownerId) != null) {
                addError("Could not save wallet", "This user already has a wallet.");
                return null;
            }
        }

        wallet.setOwner(owner);
        wallet.setBalance(balance);
        wallet.setCurrency(cleanCurrency);
        wallet.setStatus(status);

        try {
            walletDao.save(wallet);
        } catch (RuntimeException ex) {
            addError("Could not save wallet", "Please check the values and try again.");
            return null;
        }
        return "wallet-list?faces-redirect=true";
    }

    public void loadForEdit() {
        if (id != null) {
            Wallet wallet = walletDao.findById(id);
            if (wallet != null) {
                ownerId = wallet.getOwner().getId();
                balance = wallet.getBalance();
                currency = wallet.getCurrency();
                status = wallet.getStatus();
            }
        }
    }

    public String delete(Long walletId) {
        try {
            walletDao.delete(walletId);
        } catch (RuntimeException ex) {
            addError("Could not delete wallet", "Please try again.");
            return null;
        }
        return "wallet-list?faces-redirect=true";
    }

    public List<Wallet> getAllWallets() {
        return walletDao.findAll();
    }

    public List<User> getAllUsersForDropdown() {
        return userDao.findAll();
    }

    public List<String> getCurrencies() {
        return CURRENCIES;
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

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
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
