package org.imixs.workflow.office.forms;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link IBANValidator}.
 * <p>
 * {@code FacesContext} and {@code UIComponent} are mocked via Mockito since
 * the validator does not interact with them beyond receiving the value.
 */
public class IBANValidatorTest {

    private IBANValidator validator;
    private FacesContext  facesContext;
    private UIComponent   component;

    @BeforeEach
    void setUp() {
        validator = new IBANValidator();
        facesContext = mock(FacesContext.class);
        component = mock(UIComponent.class);
    }

    @Test
    void testValidIBAN_DE_unformatted() {
        assertDoesNotThrow(() ->
            validator.validate(facesContext, component, "DE91100000000123456789"));
    }

    @Test
    void testValidIBAN_GB_unformatted() {
        assertDoesNotThrow(() ->
            validator.validate(facesContext, component, "GB82WEST12345698765432"));
    }

    @Test
    void testValidIBAN_CH_unformatted() {
        assertDoesNotThrow(() ->
            validator.validate(facesContext, component, "CH9300762011623852957"));
    }

    @Test
    void testValidIBAN_DE_formatted() {
        assertDoesNotThrow(() ->
            validator.validate(facesContext, component, "DE91 1000 0000 0123 4567 89"));
    }

    @Test
    void testValidIBAN_GB_formatted() {
        assertDoesNotThrow(() ->
            validator.validate(facesContext, component, "GB82 WEST 1234 5698 7654 32"));
    }

    // --- edge cases: null and empty must not throw ---

    @Test
    void testNullValue_noException() {
        assertDoesNotThrow(() ->
            validator.validate(facesContext, component, null));
    }

    @Test
    void testEmptyValue_noException() {
        assertDoesNotThrow(() ->
            validator.validate(facesContext, component, ""));
    }

    @Test
    void testInvalidIBAN_lowercase() {
        ValidatorException ex = assertThrows(ValidatorException.class, () ->
                validator.validate(facesContext, component, "de91100000000123456789"));
        assertNotNull(ex.getFacesMessage());
        assertEquals("IBAN has invalid country code", ex.getFacesMessage().getSummary());
    }

    @Test
    void testInvalidIBAN_wrongCheckDigit() {
        ValidatorException ex = assertThrows(ValidatorException.class, () ->
            validator.validate(facesContext, component, "DE00100000000123456789"));
        assertNotNull(ex.getFacesMessage());
        assertEquals("IBAN violates ISO 7064 Mod 97-10 checksum check", ex.getFacesMessage().getSummary());
    }

    @Test
    void testInvalidIBAN_unknownCountry() {
        ValidatorException ex = assertThrows(ValidatorException.class, () ->
            validator.validate(facesContext, component, "XX91100000000123456789"));
        assertNotNull(ex.getFacesMessage());
        assertEquals("IBAN has unsupported country code", ex.getFacesMessage().getSummary());
    }

    @Test
    void testInvalidIBAN_tooShort() {
        ValidatorException ex = assertThrows(ValidatorException.class, () ->
            validator.validate(facesContext, component, "DE91"));
        assertNotNull(ex.getFacesMessage());
        assertEquals("IBAN has incorrect length", ex.getFacesMessage().getSummary());
    }

    @Test
    void testInvalidIBAN_garbage() {
        ValidatorException ex = assertThrows(ValidatorException.class, () ->
            validator.validate(facesContext, component, "NOT-AN-IBAN"));
        assertNotNull(ex.getFacesMessage());
        assertEquals("IBAN has invalid check digits", ex.getFacesMessage().getSummary());
    }

}
