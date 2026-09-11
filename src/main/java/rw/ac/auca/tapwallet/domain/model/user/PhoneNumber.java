package rw.ac.auca.tapwallet.domain.model.user;

import rw.ac.auca.tapwallet.domain.exception.DomainException;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PhoneNumber implements Serializable {

    private static final java.util.regex.Pattern FORMAT = java.util.regex.Pattern.compile("^07[2389]\\d{7}$");

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^07[2389]\\d{7}$", message = "Phone number must look like 0788123456 (MTN/Airtel format)")
    @Column(name = "phone", nullable = false, length = 10)
    private String value;

    protected PhoneNumber() {
    }

    private PhoneNumber(String value) {
        String normalised = value == null ? "" : value.trim().replace(" ", "");
        if (!FORMAT.matcher(normalised).matches()) {
            throw new DomainException("'" + value + "' is not a valid Rwandan mobile number.");
        }
        this.value = normalised;
    }

    public static PhoneNumber of(String value) {
        return new PhoneNumber(value);
    }

    public static boolean isValid(String value) {
        return value != null && FORMAT.matcher(value.trim().replace(" ", "")).matches();
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PhoneNumber)) {
            return false;
        }
        return Objects.equals(value, ((PhoneNumber) other).value);
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
