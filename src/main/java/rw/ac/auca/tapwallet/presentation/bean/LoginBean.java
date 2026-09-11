package rw.ac.auca.tapwallet.presentation.bean;

import rw.ac.auca.tapwallet.domain.exception.DomainException;
import rw.ac.auca.tapwallet.domain.model.user.User;
import rw.ac.auca.tapwallet.infrastructure.config.ServiceRegistry;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

@ManagedBean(name = "loginBean")
@RequestScoped
public class LoginBean {

    @ManagedProperty(value = "#{userSession}")
    private UserSessionBean userSession;

    private String email;
    private String password;

    public String signIn() {
        try {
            User user = ServiceRegistry.authentication().signIn(email, password);
            userSession.signIn(user);
            return user.getRole().getHomePage() + "?faces-redirect=true";
        } catch (DomainException failure) {
            addError(failure.getMessage());
            return null;
        } catch (RuntimeException failure) {
            addError("Could not sign you in. Please try again.");
            return null;
        } finally {
            password = null;
        }
    }

    private void addError(String detail) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Sign in failed", detail));
    }

    public void setUserSession(UserSessionBean userSession) {
        this.userSession = userSession;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
