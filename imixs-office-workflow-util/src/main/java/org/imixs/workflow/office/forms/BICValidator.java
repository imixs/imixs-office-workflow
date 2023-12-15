package org.imixs.workflow.office.forms;

import org.iban4j.BicFormatException;
import org.iban4j.BicUtil;
import org.iban4j.InvalidCheckDigitException;
import org.iban4j.UnsupportedCountryException;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

@FacesValidator("imixsBICValidator")
public class BICValidator implements Validator<String> {

    @Override
    public void validate(FacesContext context, UIComponent component, String value) throws ValidatorException {
        // Fügen Sie hier Ihre Validierungslogik hinzu
        if (value != null && !value.isEmpty()) {

            try {
                // strip spaces?
                if (value.contains(" ")) {
                    // yes...
                    value = value.replaceAll("\\s+", "");
                }
                BicUtil.validate(value);
            } catch (BicFormatException | InvalidCheckDigitException | UnsupportedCountryException e) {

                throw new ValidatorException(new FacesMessage(e.getMessage()));
            }

        }
    }

}
