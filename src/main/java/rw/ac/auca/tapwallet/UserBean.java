package rw.ac.auca.tapwallet;

import rw.ac.auca.tapwallet.dao.MerchantDao;
import rw.ac.auca.tapwallet.dao.UserDao;
import rw.ac.auca.tapwallet.dao.WalletDao;
import rw.ac.auca.tapwallet.model.User;
import rw.ac.auca.tapwallet.util.PasswordUtil;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import java.util.List;

/**
 * The Class UserBean.
 *
 * @author Pacifique Bakundukize
 * @version 1.0
 */
@ManagedBean
public class UserBean {

    private UserDao userDao = new UserDao();
    private WalletDao walletDao = new WalletDao();
    private MerchantDao merchantDao = new MerchantDao();

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private String role = "CUSTOMER";
    private String status = "ACTIVE";

    public String save() {
        String cleanName = fullName == null ? null : fullName.trim();
        String cleanEmail = email == null ? null : email.trim().toLowerCase();
        String cleanPhone = phone == null ? null : phone.trim();

        if (id == null && (password == null || password.length() < 6)) {
            addError("Could not save user", "Password must be at least 6 characters.");
            return null;
        }
        if (password != null && !password.isEmpty() && password.length() < 6) {
            addError("Could not save user", "Password must be at least 6 characters.");
            return null;
        }

        User existing = userDao.findByEmail(cleanEmail);
        if (existing != null && (id == null || !existing.getId().equals(id))) {
            addError("Could not save user", "That email is already registered.");
            return null;
        }

        User user;
        if (id != null) {
            user = userDao.findById(id);
            if (user == null) {
                return "user-list?faces-redirect=true";
            }
        } else {
            user = new User();
        }

        user.setFullName(cleanName);
        user.setEmail(cleanEmail);
        user.setPhone(cleanPhone);
        user.setRole(role);
        user.setStatus(status);

        if (password != null && !password.trim().isEmpty()) {
            user.setPasswordHash(PasswordUtil.hash(password));
        } else if (user.getPasswordHash() == null) {
            addError("Could not save user", "Password is required.");
            return null;
        }

        try {
            userDao.save(user);
        } catch (RuntimeException ex) {
            // Never leak a stack trace to the page — show a friendly message and stay put.
            addError("Could not save user", "Please check the values (e.g. email must be unique) and try again.");
            return null;
        }
        password = null;
        return "user-list?faces-redirect=true";
    }

    public void loadForEdit() {
        if (id != null) {
            User user = userDao.findById(id);
            if (user != null) {
                fullName = user.getFullName();
                email = user.getEmail();
                phone = user.getPhone();
                role = user.getRole();
                status = user.getStatus();
            }
        }
    }

    public String delete(Long userId) {
        if (walletDao.findByOwner(userId) != null) {
            addError("Could not delete user", "This user still owns a wallet. Delete the wallet first.");
            return null;
        }
        if (merchantDao.findByOperator(userId) != null) {
            addError("Could not delete user", "This user still operates a shop. Delete the merchant first.");
            return null;
        }
        try {
            userDao.delete(userId);
        } catch (RuntimeException ex) {
            addError("Could not delete user", "This user is still referenced by other records.");
            return null;
        }
        return "user-list?faces-redirect=true";
    }

    public List<User> getAllUsers() {
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
