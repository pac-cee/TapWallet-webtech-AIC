package rw.ac.auca.tapwallet;

import rw.ac.auca.tapwallet.dao.WalletDao;
import rw.ac.auca.tapwallet.model.Wallet;
import rw.ac.auca.tapwallet.model.Withdrawal;
import rw.ac.auca.tapwallet.service.WithdrawalService;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * The Class WithdrawalBean.
 *
 * @author Pacifique Bakundukize
 * @version 1.0
 */
@ManagedBean
public class WithdrawalBean {

    private WithdrawalService withdrawalService = new WithdrawalService();
    private WalletDao walletDao = new WalletDao();

    private Long walletId;
    private BigDecimal amount;
    private String method = "MOMO";

    public static final List<String> METHODS = Arrays.asList("MOMO", "CASH", "BANK");

    public String save() {
        if (walletId == null) {
            addError("Could not withdraw", "Please choose a wallet.");
            return null;
        }
        if (amount == null || amount.compareTo(new BigDecimal("0.01")) < 0) {
            addError("Could not withdraw", "Amount must be at least 0.01.");
            return null;
        }
        Wallet wallet = walletDao.findById(walletId);
        if (wallet == null) {
            addError("Could not withdraw", "The chosen wallet no longer exists.");
            return null;
        }
        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setWallet(wallet);
        withdrawal.setAmount(amount);
        withdrawal.setMethod(method);
        try {
            withdrawalService.debit(withdrawal);
        } catch (IllegalStateException | IllegalArgumentException ex) {
            addError("Could not withdraw", ex.getMessage());
            return null;
        } catch (RuntimeException ex) {
            addError("Could not withdraw", "Please check the values and try again.");
            return null;
        }
        return "withdrawal-list?faces-redirect=true";
    }

    public String delete(Long withdrawalId) {
        try {
            withdrawalService.undo(withdrawalId);
        } catch (IllegalStateException ex) {
            addError("Could not undo withdrawal", ex.getMessage());
            return null;
        } catch (RuntimeException ex) {
            addError("Could not undo withdrawal", "Please try again.");
            return null;
        }
        return "withdrawal-list?faces-redirect=true";
    }

    public List<Withdrawal> getAllWithdrawals() {
        return withdrawalService.findAll();
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
