package rw.ac.auca.tapwallet.domain.model.merchant;

import rw.ac.auca.tapwallet.domain.exception.DomainException;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class MerchantCode implements Serializable {

    private static final java.util.regex.Pattern FORMAT = java.util.regex.Pattern.compile("^[A-Z]{3}-\\d{3}$");

    @NotBlank(message = "Merchant code is required")
    @Pattern(regexp = "^[A-Z]{3}-\\d{3}$", message = "Merchant code must look like CAF-001")
    @Column(name = "merchant_code", nullable = false, unique = true, length = 7)
    private String value;

    protected MerchantCode() {
    }

    private MerchantCode(String value) {
        String normalised = value == null ? "" : value.trim().toUpperCase();
        if (!FORMAT.matcher(normalised).matches()) {
            throw new DomainException("'" + value + "' is not a valid merchant code (expected CAF-001).");
        }
        this.value = normalised;
    }

    public static MerchantCode of(String value) {
        return new MerchantCode(value);
    }

    public static boolean isValid(String value) {
        return value != null && FORMAT.matcher(value.trim().toUpperCase()).matches();
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MerchantCode)) {
            return false;
        }
        return Objects.equals(value, ((MerchantCode) other).value);
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
