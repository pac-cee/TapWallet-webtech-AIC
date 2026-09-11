package rw.ac.auca.tapwallet.presentation.validator;

import rw.ac.auca.tapwallet.domain.model.user.PhoneNumber;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 * Custom JSF validator (validation type 2 of 3). It delegates to the PhoneNumber
 * value object, so the browser form and the domain model can never disagree about
 * what a valid number is.
 */
@FacesValidator("phoneValidator")
public class PhoneValidator implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        String phone = value == null ? "" : value.toString();
        if (!PhoneNumber.isValid(phone)) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Invalid phone number", "Phone number must look like 0788123456 (MTN/Airtel format)."));
        }
    }
}
