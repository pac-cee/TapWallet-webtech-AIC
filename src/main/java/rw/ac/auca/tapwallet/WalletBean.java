package rw.ac.auca.tapwallet;

import rw.ac.auca.tapwallet.dao.UserDao;
import rw.ac.auca.tapwallet.dao.WalletDao;
import rw.ac.auca.tapwallet.model.User;
import rw.ac.auca.tapwallet.model.Wallet;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import java.math.BigDecimal;
import java.util.List;

/**
 * The Class WalletBean.
 *
 * @author Pacifique Bakundukize
 * @version 1.0
 */
@ManagedBean
public class WalletBean {

    private WalletDao walletDao = new WalletDao();
    private UserDao userDao = new UserDao();

    private Long id;
    private Long ownerId;
    private BigDecimal balance = BigDecimal.ZERO;
    private String currency = "RWF";
    private String status = "ACTIVE";

    public String save() {
        Wallet wallet;
        if (id != null) {
            wallet = walletDao.findById(id);
            if (wallet == null) {
                return "wallet-list?faces-redirect=true";
            }
        } else {
            wallet = new Wallet();
        }

        User owner = userDao.findById(ownerId);
        wallet.setOwner(owner);
        wallet.setBalance(balance);
        wallet.setCurrency(currency);
        wallet.setStatus(status);

        try {
            walletDao.save(wallet);
        } catch (RuntimeException ex) {
            // Never leak a stack trace to the page — show a friendly message and stay put.
            if (FacesContext.getCurrentInstance() != null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Could not save wallet", "Please check the values and try again."));
            }
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
        walletDao.delete(walletId);
        return "wallet-list?faces-redirect=true";
    }

    public List<Wallet> getAllWallets() {
        return walletDao.findAll();
    }

    public List<User> getAllUsersForDropdown() {
        return userDao.findAll();
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
