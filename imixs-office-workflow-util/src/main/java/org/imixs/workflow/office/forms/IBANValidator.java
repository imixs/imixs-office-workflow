package org.imixs.workflow.office.forms;

import org.iban4j.IbanFormat;
import org.iban4j.IbanFormatException;
import org.iban4j.IbanUtil;
import org.iban4j.InvalidCheckDigitException;
import org.iban4j.UnsupportedCountryException;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

@FacesValidator("imixsIBANValidator")
public class IBANValidator implements Validator<String> {

    @Override
    public void validate(FacesContext context, UIComponent component, String value) throws ValidatorException {
        // Fügen Sie hier Ihre Validierungslogik hinzu
        if (value != null && !value.isEmpty()) {

            try {
                if (value.contains(" ")) {
                    // formated
                    IbanUtil.validate(value, IbanFormat.Default);
                } else {
                    // normal
                    IbanUtil.validate(value, IbanFormat.None);
                }
            } catch (IbanFormatException | InvalidCheckDigitException | UnsupportedCountryException e) {

                throw new ValidatorException(new FacesMessage(e.getMessage()));
            }

        }
    }

}
