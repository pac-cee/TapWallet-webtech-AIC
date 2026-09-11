package rw.ac.auca.tapwallet.presentation.bean;

import rw.ac.auca.tapwallet.domain.exception.DomainException;
import rw.ac.auca.tapwallet.domain.model.merchant.Merchant;
import rw.ac.auca.tapwallet.domain.model.shared.AccountStatus;
import rw.ac.auca.tapwallet.domain.model.shared.Currency;
import rw.ac.auca.tapwallet.domain.model.user.Role;
import rw.ac.auca.tapwallet.domain.model.user.User;
import rw.ac.auca.tapwallet.infrastructure.config.ServiceRegistry;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.List;

/**
 * Administrator screen for merchants: the full Create / Read / Update / Delete cycle.
 */
@ManagedBean(name = "merchantAdmin")
@ViewScoped
public class MerchantAdminBean implements Serializable {

    private Long id;
    private String businessName;
    private String code;
    private Long operatorId;
    private AccountStatus status = AccountStatus.ACTIVE;

    public List<Merchant> getMerchants() {
        return ServiceRegistry.merchants().findAll();
    }

    /** Only users with the MERCHANT role may operate a shop. */
    public List<User> getOperatorChoices() {
        return ServiceRegistry.users().findByRole(Role.MERCHANT);
    }

    public AccountStatus[] getStatuses() {
        return AccountStatus.values();
    }

    public void loadForEdit() {
        if (id != null) {
            ServiceRegistry.merchants().findById(id).ifPresent(merchant -> {
                businessName = merchant.getBusinessName();
                code = merchant.getCode().getValue();
                operatorId = merchant.getOperator().getId();
                status = merchant.getStatus();
            });
        }
    }

    public String save() {
        try {
            if (id == null) {
                ServiceRegistry.merchants().register(businessName, code, operatorId, Currency.RWF);
            } else {
                ServiceRegistry.merchants().update(id, businessName, code, status);
            }
            return "/admin/merchants.xhtml?faces-redirect=true";
        } catch (DomainException failure) {
            addError("Could not save merchant", failure.getMessage());
            return null;
        } catch (RuntimeException failure) {
            addError("Could not save merchant", "Please check the values and try again.");
            return null;
        }
    }

    public String delete(Long merchantId) {
        try {
            ServiceRegistry.merchants().delete(merchantId);
            return "/admin/merchants.xhtml?faces-redirect=true";
        } catch (DomainException failure) {
            addError("Could not delete merchant", failure.getMessage());
            return null;
        } catch (RuntimeException failure) {
            addError("Could not delete merchant", "This merchant is still referenced by other records.");
            return null;
        }
    }

    public boolean isCreating() {
        return id == null;
    }

    private void addError(String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, detail));
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }
}
