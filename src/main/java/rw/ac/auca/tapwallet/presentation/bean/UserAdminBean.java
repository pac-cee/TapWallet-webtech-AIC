package rw.ac.auca.tapwallet.presentation.bean;

import rw.ac.auca.tapwallet.domain.exception.DomainException;
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
import java.math.BigDecimal;
import java.util.List;

/**
 * Administrator screen for users: the full Create / Read / Update / Delete cycle.
 */
@ManagedBean(name = "userAdmin")
@ViewScoped
public class UserAdminBean implements Serializable {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private Role role = Role.CUSTOMER;
    private AccountStatus status = AccountStatus.ACTIVE;
    private BigDecimal openingBalance = BigDecimal.ZERO;
    private Currency currency = Currency.RWF;

    public List<User> getUsers() {
        return ServiceRegistry.users().findAll();
    }

    public Role[] getRoles() {
        return Role.values();
    }

    public AccountStatus[] getStatuses() {
        return AccountStatus.values();
    }

    public Currency[] getCurrencies() {
        return Currency.values();
    }

    public void loadForEdit() {
        if (id != null) {
            ServiceRegistry.users().findById(id).ifPresent(user -> {
                fullName = user.getFullName();
                email = user.getEmail().getValue();
                phone = user.getPhone().getValue();
                role = user.getRole();
                status = user.getStatus();
            });
        }
    }

    public String save() {
        try {
            if (id == null) {
                ServiceRegistry.users().register(fullName, email, phone, password, role, openingBalance, currency);
            } else {
                ServiceRegistry.users().update(id, fullName, email, phone, password, status);
            }
            return "/admin/users.xhtml?faces-redirect=true";
        } catch (DomainException failure) {
            addError("Could not save user", failure.getMessage());
            return null;
        } catch (RuntimeException failure) {
            addError("Could not save user", "Please check the values and try again.");
            return null;
        } finally {
            password = null;
        }
    }

    public String delete(Long userId) {
        try {
            ServiceRegistry.users().delete(userId);
            return "/admin/users.xhtml?faces-redirect=true";
        } catch (DomainException failure) {
            addError("Could not delete user", failure.getMessage());
            return null;
        } catch (RuntimeException failure) {
            addError("Could not delete user", "This user is still referenced by other records.");
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(BigDecimal openingBalance) {
        this.openingBalance = openingBalance;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
}
