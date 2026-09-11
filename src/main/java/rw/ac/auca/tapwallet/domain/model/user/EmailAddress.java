package rw.ac.auca.tapwallet.domain.model.user;

import rw.ac.auca.tapwallet.domain.exception.DomainException;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

@Embeddable
public class EmailAddress implements Serializable {

    private static final Pattern FORMAT = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[A-Za-z]{2,}$");

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid address")
    @Column(name = "email", nullable = false, unique = true, length = 120)
    private String value;

    protected EmailAddress() {
    }

    private EmailAddress(String value) {
        String normalised = value == null ? "" : value.trim().toLowerCase();
        if (!FORMAT.matcher(normalised).matches()) {
            throw new DomainException("'" + value + "' is not a valid email address.");
        }
        this.value = normalised;
    }

    public static EmailAddress of(String value) {
        return new EmailAddress(value);
    }

    public static boolean isValid(String value) {
        return value != null && FORMAT.matcher(value.trim().toLowerCase()).matches();
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof EmailAddress)) {
            return false;
        }
        return Objects.equals(value, ((EmailAddress) other).value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
