package org.imixs.workflow.office.forms;

import de.speedbanking.bic.Bic;
import de.speedbanking.bic.InvalidBicException;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

/**
 * JSF validator for BIC (ISO 9362) inputs.
 * <p>
 * Delegates validation to {@link Bic#of(CharSequence)} from the
 * <em>iban-commons</em> library (de.speedbanking). Both BIC-8
 * ("MARKDEFF") and BIC-11 ("MARKDEFFXXX") formats are accepted.
 * Whitespace is stripped before validation.
 *
 * @see <a href="https://github.com/SpeedBankingDe/iban-commons">iban-commons</a>
 */
@FacesValidator("imixsBICValidator")
public class BICValidator implements Validator<String> {

    @Override
    public void validate(FacesContext context, UIComponent component, String value) throws ValidatorException {
        if (value != null && !value.isEmpty()) {

            try {
                Bic.of(value.replaceAll("\\s+", ""));
            } catch (InvalidBicException ex) {
                throw new ValidatorException(new FacesMessage(ex.getMessage()), ex);
            }

        }
    }

}
