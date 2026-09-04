package rw.ac.auca.tapwallet;

import rw.ac.auca.tapwallet.dao.TopUpDao;
import rw.ac.auca.tapwallet.dao.WalletDao;
import rw.ac.auca.tapwallet.model.TopUp;
import rw.ac.auca.tapwallet.model.Wallet;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * The Class TopUpBean.
 *
 * @author Pacifique Bakundukize
 * @version 1.0
 */
@ManagedBean
public class TopUpBean {

    private TopUpDao topUpDao = new TopUpDao();
    private WalletDao walletDao = new WalletDao();

    private Long walletId;
    private BigDecimal amount;
    private String method = "MOMO";

    public static final List<String> METHODS = Arrays.asList("MOMO", "CARD", "BANK");

    public String save() {
        if (walletId == null) {
            addError("Could not top up", "Please choose a wallet.");
            return null;
        }
        if (amount == null || amount.compareTo(new BigDecimal("0.01")) < 0) {
            addError("Could not top up", "Amount must be at least 0.01.");
            return null;
        }
        Wallet wallet = walletDao.findById(walletId);
        if (wallet == null) {
            addError("Could not top up", "The chosen wallet no longer exists.");
            return null;
        }
        TopUp topUp = new TopUp();
        topUp.setWallet(wallet);
        topUp.setAmount(amount);
        topUp.setMethod(method);
        try {
            topUpDao.credit(topUp);
        } catch (IllegalStateException | IllegalArgumentException ex) {
            addError("Could not top up", ex.getMessage());
            return null;
        } catch (RuntimeException ex) {
            addError("Could not top up", "Please check the values and try again.");
            return null;
        }
        return "topup-list?faces-redirect=true";
    }

    public String delete(Long topUpId) {
        try {
            topUpDao.deleteWithReversal(topUpId);
        } catch (IllegalStateException ex) {
            addError("Could not undo top-up", ex.getMessage());
            return null;
        } catch (RuntimeException ex) {
            addError("Could not undo top-up", "Please try again.");
            return null;
        }
        return "topup-list?faces-redirect=true";
    }

    public List<TopUp> getAllTopUps() {
        return topUpDao.findAll();
    }

    public List<Wallet> getAllWalletsForDropdown() {
        return walletDao.findAll();
    }

    public List<String> getMethods() {
        return METHODS;
    }

    private void addError(String summary, String detail) {
        if (FacesContext.getCurrentInstance() != null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, detail));
        }
    }

    public Long getWalletId() {
        return walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
