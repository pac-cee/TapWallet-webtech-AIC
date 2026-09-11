package rw.ac.auca.tapwallet.presentation.bean;

import rw.ac.auca.tapwallet.domain.model.user.Role;
import rw.ac.auca.tapwallet.domain.model.user.User;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.Serializable;

/**
 * Holds who is signed in for the life of the HTTP session. Every other bean asks
 * this one for the current user id rather than trusting anything from the request.
 */
@ManagedBean(name = "userSession")
@SessionScoped
public class UserSessionBean implements Serializable {

    public static final String SESSION_KEY = "tapwallet.currentUser";

    private Long userId;
    private String fullName;
    private String email;
    private Role role;

    public void signIn(User user) {
        this.userId = user.getId();
        this.fullName = user.getFullName();
        this.email = user.getEmail().getValue();
        this.role = user.getRole();
        httpSession(true).setAttribute(SESSION_KEY, user.getId());
        httpSession(true).setAttribute(SESSION_KEY + ".role", user.getRole().name());
    }

    public String signOut() {
        this.userId = null;
        this.fullName = null;
        this.email = null;
        this.role = null;
        HttpSession session = httpSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "/login.xhtml?faces-redirect=true";
    }

    public boolean isSignedIn() {
        return userId != null;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isCustomer() {
        return role == Role.CUSTOMER;
    }

    public boolean isMerchant() {
        return role == Role.MERCHANT;
    }

    public String getHomePage() {
        return role == null ? "/login.xhtml" : role.getHomePage();
    }

    public String getRoleLabel() {
        return role == null ? "" : role.getLabel();
    }

    private HttpSession httpSession(boolean create) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
            return null;
        }
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        return request.getSession(create);
    }

    public Long getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }
}
