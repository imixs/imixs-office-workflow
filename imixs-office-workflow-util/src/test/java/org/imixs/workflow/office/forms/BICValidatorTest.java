package org.imixs.workflow.office.forms;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link BICValidator}.
 * <p>
 * {@code FacesContext} and {@code UIComponent} are mocked via Mockito since
 * the validator does not interact with them beyond receiving the value.
 */
public class BICValidatorTest {

    private BICValidator validator;
    private FacesContext facesContext;
    private UIComponent  component;

    @BeforeEach
    void setUp() {
        validator = new BICValidator();
        facesContext = mock(FacesContext.class);
        component = mock(UIComponent.class);
    }

    @Test
    void testValidBIC8_DeutscheBundesbank() {
        assertDoesNotThrow(() ->
            validator.validate(facesContext, component, "MARKDEFF"));
    }

    @Test
    void testValidBIC8_Commerzbank() {
        assertDoesNotThrow(() ->
            validator.validate(facesContext, component, "COBADEFF"));
    }

    @Test
    void testValidBIC11_withBranchCode() {
        assertDoesNotThrow(() ->
            validator.validate(facesContext, component, "PALSPS22XXX"));
    }

    @Test
    void testValidBIC11_DeutscheBundesbank() {
        assertDoesNotThrow(() ->
            validator.validate(facesContext, component, "MARKDEFFXXX"));
    }

    @Test
    void testValidBIC_withSpaces() {
        assertDoesNotThrow(() ->
            validator.validate(facesContext, component, "MARK DEFF"));
    }

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
    void testInvalidBIC_tooShort() {
        ValidatorException ex = assertThrows(ValidatorException.class, () ->
            validator.validate(facesContext, component, "MARK"));
        assertNotNull(ex.getFacesMessage());
        assertEquals("BIC has incorrect length", ex.getFacesMessage().getSummary());
    }

    @Test
    void testInvalidBIC_invalidCountryCode() {
        ValidatorException ex = assertThrows(ValidatorException.class, () ->
            validator.validate(facesContext, component, "MARK11FF"));
        assertNotNull(ex.getFacesMessage());
        assertEquals("BIC has invalid country code", ex.getFacesMessage().getSummary());
    }

    @Test
    void testInvalidBIC_garbage() {
        ValidatorException ex = assertThrows(ValidatorException.class, () ->
            validator.validate(facesContext, component, "NOT-A-BIC"));
        assertNotNull(ex.getFacesMessage());
        assertEquals("BIC has incorrect length", ex.getFacesMessage().getSummary());
    }

}
