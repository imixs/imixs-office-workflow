package org.imixs.workflow.office.forms;

import de.speedbanking.iban.Iban;
import de.speedbanking.iban.InvalidIbanException;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

/**
 * JSF validator for IBAN (ISO 13616) inputs.
 * <p>
 * Delegates validation to {@link Iban#of(CharSequence)} from the
 * <em>iban-commons</em> library (de.speedbanking). The library automatically
 * handles whitespace, so both formatted ("DE91 1000 0000 0123 4567 89") 
 * and unformatted ("DE91100000000123456789") input is accepted.
 *
 * @see <a href="https://github.com/SpeedBankingDe/iban-commons">iban-commons</a>
 */
@FacesValidator("imixsIBANValidator")
public class IBANValidator implements Validator<String> {

    @Override
    public void validate(FacesContext context, UIComponent component, String value) throws ValidatorException {
        if (value != null && !value.isEmpty()) {

            try {
                Iban.of(value);
            } catch (InvalidIbanException ex) {
                throw new ValidatorException(new FacesMessage(ex.getMessage()), ex);
            }

        }
    }

}
