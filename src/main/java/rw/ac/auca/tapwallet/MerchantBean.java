package rw.ac.auca.tapwallet;

import rw.ac.auca.tapwallet.dao.MerchantDao;
import rw.ac.auca.tapwallet.dao.UserDao;
import rw.ac.auca.tapwallet.model.Merchant;
import rw.ac.auca.tapwallet.model.User;
import rw.ac.auca.tapwallet.service.PaymentService;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import java.util.List;

@ManagedBean
public class MerchantBean {
    private MerchantDao merchantDao = new MerchantDao();
    private UserDao userDao = new UserDao();
    private PaymentService paymentService = new PaymentService();

    private Long id;
    private String businessName;
    private String merchantCode;
    private Long operatorId;
    private String status = "ACTIVE";

    public String save() {
        String cleanName = businessName == null ? null : businessName.trim();
        String cleanCode = merchantCode == null ? null : merchantCode.trim().toUpperCase();

        if (operatorId == null) {
            addError("Could not save merchant", "Please choose an operator.");
            return null;
        }

        Merchant byCode = merchantDao.findByCode(cleanCode);
        if (byCode != null && (id == null || !byCode.getId().equals(id))) {
            addError("Could not save merchant", "That merchant code is already taken.");
            return null;
        }

        User operator = userDao.findById(operatorId);
        if (operator == null) {
            addError("Could not save merchant", "The chosen operator no longer exists.");
            return null;
        }

        Merchant merchant;
        if (id != null) {
            merchant = merchantDao.findById(id);
            if (merchant == null) {
                return "merchant-list?faces-redirect=true";
            }
        } else {
            merchant = new Merchant();
            if (merchantDao.findByOperator(operatorId) != null) {
                addError("Could not save merchant", "This user already operates a shop.");
                return null;
            }
        }

        merchant.setBusinessName(cleanName);
        merchant.setMerchantCode(cleanCode);
        merchant.setOperator(operator);
        merchant.setStatus(status);

        try {
            merchantDao.save(merchant);
        } catch (RuntimeException ex) {
            addError("Could not save merchant", "Please check the values and try again.");
            return null;
        }
        return "merchant-list?faces-redirect=true";
    }

    public void loadForEdit() {
        if (id != null) {
            Merchant merchant = merchantDao.findById(id);
            if (merchant != null) {
                businessName = merchant.getBusinessName();
                merchantCode = merchant.getMerchantCode();
                operatorId = merchant.getOperator().getId();
                status = merchant.getStatus();
            }
        }
    }

    public String delete(Long merchantId) {
        if (merchantId != null && !paymentService.findByMerchant(merchantId).isEmpty()) {
            addError("Could not delete merchant", "This merchant still has payment history. Reverse those payments first.");
            return null;
        }
        try {
            merchantDao.delete(merchantId);
        } catch (RuntimeException ex) {
            addError("Could not delete merchant", "Please try again.");
            return null;
        }
        return "merchant-list?faces-redirect=true";
    }

    public List<Merchant> getAllMerchants() {
        return merchantDao.findAll();
    }

    public List<User> getAllUsersForDropdown() {
        return userDao.findAll();
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

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getMerchantCode() {
        return merchantCode;
    }

    public void setMerchantCode(String merchantCode) {
        this.merchantCode = merchantCode;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
