package rw.ac.auca.tapwallet.util;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.util.regex.Pattern;

@FacesValidator("phoneValidator")
public class PhoneValidator implements Validator {
    private static final Pattern RWANDAN_PHONE = Pattern.compile("^07[2389]\\d{7}$");

    public static boolean isValidPhone(String phone) {
        return phone != null && RWANDAN_PHONE.matcher(phone).matches();
    }

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        String phone = value == null ? "" : value.toString();
        if (!isValidPhone(phone)) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Invalid phone number", "Phone number must look like 0788123456 (MTN/Airtel format).");
            throw new ValidatorException(message);
        }
    }
}
