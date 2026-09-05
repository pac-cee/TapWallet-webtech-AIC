package rw.ac.auca.tapwallet;

import rw.ac.auca.tapwallet.dao.MerchantDao;
import rw.ac.auca.tapwallet.dao.WalletDao;
import rw.ac.auca.tapwallet.model.Merchant;
import rw.ac.auca.tapwallet.model.Transaction;
import rw.ac.auca.tapwallet.model.Wallet;
import rw.ac.auca.tapwallet.service.PaymentService;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@ManagedBean
public class TransactionBean {
    private PaymentService paymentService = new PaymentService();
    private WalletDao walletDao = new WalletDao();
    private MerchantDao merchantDao = new MerchantDao();

    private Long senderWalletId;
    private Long merchantId;
    private BigDecimal amount;

    public String save() {
        if (senderWalletId == null || merchantId == null) {
            addError("Could not send payment", "Please choose both a wallet and a merchant.");
            return null;
        }
        if (amount == null || amount.compareTo(new BigDecimal("0.01")) < 0) {
            addError("Could not send payment", "Amount must be at least 0.01.");
            return null;
        }

        Wallet sender = walletDao.findById(senderWalletId);
        Merchant merchant = merchantDao.findById(merchantId);
        if (sender == null || merchant == null) {
            addError("Could not send payment", "The wallet or the merchant no longer exists.");
            return null;
        }

        Transaction tx = new Transaction();
        tx.setSenderWallet(sender);
        tx.setReceiverMerchant(merchant);
        tx.setAmount(amount);
        tx.setStatus("COMPLETED");

        try {
            paymentService.pay(tx);
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

    public List<Merchant> getActiveMerchantsForDropdown() {
        return merchantDao.findAll().stream()
                .filter(m -> "ACTIVE".equals(m.getStatus()))
                .collect(Collectors.toList());
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

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
