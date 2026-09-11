package rw.ac.auca.tapwallet.domain.model.user;

import rw.ac.auca.tapwallet.domain.exception.DomainException;
import rw.ac.auca.tapwallet.domain.model.shared.AccountStatus;
import rw.ac.auca.tapwallet.domain.model.shared.AuditableEntity;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Aggregate root for a person who can sign in to TapWallet.
 */
@Entity
@Table(name = "app_user")
public class User extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 60, message = "Full name must be between 3 and 60 characters")
    @Column(name = "full_name", nullable = false, length = 60)
    private String fullName;

    @Valid
    @NotNull
    @Embedded
    private EmailAddress email;

    @Valid
    @NotNull
    @Embedded
    private PhoneNumber phone;

    @NotBlank(message = "Password is required")
    @Column(name = "password_hash", nullable = false, length = 200)
    private String passwordHash;

    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 16)
    private Role role;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private AccountStatus status = AccountStatus.ACTIVE;

    protected User() {
    }

    private User(String fullName, EmailAddress email, PhoneNumber phone, String passwordHash, Role role) {
        rename(fullName);
        changeEmail(email);
        changePhone(phone);
        changePasswordHash(passwordHash);
        assignRole(role);
        this.status = AccountStatus.ACTIVE;
    }

    /** Factory method — the only supported way to create a User. */
    public static User register(String fullName, EmailAddress email, PhoneNumber phone, String passwordHash, Role role) {
        return new User(fullName, email, phone, passwordHash, role);
    }

    public void rename(String newFullName) {
        String trimmed = newFullName == null ? "" : newFullName.trim();
        if (trimmed.length() < 3) {
            throw new DomainException("Full name must be at least 3 characters.");
        }
        this.fullName = trimmed;
    }

    public void changeEmail(EmailAddress newEmail) {
        if (newEmail == null) {
            throw new DomainException("Email is required.");
        }
        this.email = newEmail;
    }

    public void changePhone(PhoneNumber newPhone) {
        if (newPhone == null) {
            throw new DomainException("Phone number is required.");
        }
        this.phone = newPhone;
    }

    public void changePasswordHash(String newPasswordHash) {
        if (newPasswordHash == null || newPasswordHash.trim().isEmpty()) {
            throw new DomainException("Password is required.");
        }
        this.passwordHash = newPasswordHash;
    }

    public void assignRole(Role newRole) {
        if (newRole == null) {
            throw new DomainException("Role is required.");
        }
        this.role = newRole;
    }

    public void freeze() {
        this.status = AccountStatus.FROZEN;
    }

    public void activate() {
        this.status = AccountStatus.ACTIVE;
    }

    public boolean canSignIn() {
        return status.isActive();
    }

    public boolean hasRole(Role candidate) {
        return role == candidate;
    }

    /**
     * Identity comparison that also holds before the user has been persisted.
     * Two references to the same person must compare equal even while the
     * database id is still null, otherwise rules such as "you cannot pay your
     * own shop" would silently pass on a freshly built object.
     */
    public boolean isSameAs(User other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        return id != null && id.equals(other.id);
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public EmailAddress getEmail() {
        return email;
    }

    public PhoneNumber getPhone() {
        return phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public AccountStatus getStatus() {
        return status;
    }
}
