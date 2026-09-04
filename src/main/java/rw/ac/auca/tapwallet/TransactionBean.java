package rw.ac.auca.tapwallet;

import rw.ac.auca.tapwallet.dao.WalletDao;
import rw.ac.auca.tapwallet.model.Transaction;
import rw.ac.auca.tapwallet.model.Wallet;
import rw.ac.auca.tapwallet.service.PaymentService;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * The Class TransactionBean.
 *
 * <p>Ledger entries are created and (with automatic reversal) deleted —
 * never edited, so the money trail stays trustworthy.</p>
 *
 * @author Pacifique Bakundukize
 * @version 1.0
 */
@ManagedBean
public class TransactionBean {

    private PaymentService paymentService = new PaymentService();
    private WalletDao walletDao = new WalletDao();

    private Long senderWalletId;
    private Long receiverWalletId;
    private BigDecimal amount;
    private String type = "PAYMENT";

    public static final List<String> TYPES = Arrays.asList("PAYMENT", "TRANSFER");

    public String save() {
        if (senderWalletId == null || receiverWalletId == null) {
            addError("Could not send payment", "Please choose both wallets.");
            return null;
        }
        if (senderWalletId.equals(receiverWalletId)) {
            addError("Could not send payment", "Sender and receiver must be different wallets.");
            return null;
        }
        if (amount == null || amount.compareTo(new BigDecimal("0.01")) < 0) {
            addError("Could not send payment", "Amount must be at least 0.01.");
            return null;
        }

        Wallet sender = walletDao.findById(senderWalletId);
        Wallet receiver = walletDao.findById(receiverWalletId);
        if (sender == null || receiver == null) {
            addError("Could not send payment", "One of the wallets no longer exists.");
            return null;
        }

        Transaction tx = new Transaction();
        tx.setSenderWallet(sender);
        tx.setReceiverWallet(receiver);
        tx.setAmount(amount);
        tx.setType(type);
        tx.setStatus("COMPLETED");

        try {
            paymentService.transfer(tx);
        } catch (IllegalStateException | IllegalArgumentException ex) {
            addError("Could not send payment", ex.getMessage());
            return null;
        } catch (RuntimeException ex) {
            addError("Could not send payment", "Please check the values and try again.");
            return null;
        }
        return "transaction-list?faces-redirect=true";
    }

    public String delete(Long transactionId) {
        try {
            paymentService.reverse(transactionId);
        } catch (IllegalStateException ex) {
            addError("Could not reverse payment", ex.getMessage());
            return null;
        } catch (RuntimeException ex) {
            addError("Could not reverse payment", "Please try again.");
            return null;
        }
        return "transaction-list?faces-redirect=true";
    }

    public List<Transaction> getAllTransactions() {
        return paymentService.findAll();
    }

    public List<Wallet> getAllWalletsForDropdown() {
        return walletDao.findAll();
    }

    public List<String> getTypes() {
        return TYPES;
    }

    private void addError(String summary, String detail) {
        if (FacesContext.getCurrentInstance() != null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, detail));
        }
    }

    public Long getSenderWalletId() {
        return senderWalletId;
    }

    public void setSenderWalletId(Long senderWalletId) {
        this.senderWalletId = senderWalletId;
    }

    public Long getReceiverWalletId() {
        return receiverWalletId;
    }

    public void setReceiverWalletId(Long receiverWalletId) {
        this.receiverWalletId = receiverWalletId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
