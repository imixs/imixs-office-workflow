/*******************************************************************************
 *  Imixs Workflow 
 *  Copyright (C) 2001, 2011 Imixs Software Solutions GmbH,  
 *  http://www.imixs.com
 *  
 *  This program is free software; you can redistribute it and/or 
 *  modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation; either version 2 
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 *  General Public License for more details.
 *  
 *  You can receive a copy of the GNU General Public
 *  License at http://www.gnu.org/licenses/gpl.html
 *  
 *  Project: 
 *  	http://www.imixs.org
 *  	http://java.net/projects/imixs-workflow
 *  
 *  Contributors:  
 *  	Imixs Software Solutions GmbH - initial API and implementation
 *  	Ralph Soika - Software Developer
 *******************************************************************************/

package org.imixs.workflow.office.forms;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.faces.data.WorkflowEvent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.faces.application.Application;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * The CustomFormController is used by the workitem form to compute a set of
 * fields based on a data object provided by the model.
 * 
 * The form definition is automatically updated in a workitem field
 * 'txtWorkflowEditorCustomForm' by the {@link CustomFormService} EJB.
 * 
 * @author rsoika
 *
 */
@Named
@ConversationScoped
public class CustomFormController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(CustomFormService.class.getName());

    private List<CustomSubForm> subforms = null;
    private List<CustomFormSection> sections;
    private String supportedExpressions = "";

    @Inject
    CustomFormService customFormService;

    public CustomFormController() {
        super();
    }

    public List<CustomSubForm> getSubforms() {
        return subforms;
    }

    public List<CustomFormSection> getSections() {
        return sections;
    }

    /**
     * WorkflowEvent listener to update the current FormDefinition.
     * 
     * @param workflowEvent
     * @throws AccessDeniedException
     * @throws ModelException
     */
    public void onWorkflowEvent(@Observes WorkflowEvent workflowEvent) throws ModelException {
        if (workflowEvent == null)
            return;

        // skip if not a workItem...
        if (workflowEvent.getWorkitem() != null
                && !workflowEvent.getWorkitem().getItemValueString("type").startsWith("workitem")) {
            return;
        }

        int eventType = workflowEvent.getEventType();
        if (WorkflowEvent.WORKITEM_CHANGED == eventType || WorkflowEvent.WORKITEM_CREATED == eventType) {
            // parse form definition
            loadCustomFormExpressions();
            computeFieldDefinition(workflowEvent.getWorkitem());
        }

    }

    /**
     * The method reads the custom form definition from the item
     * 'txtWorkflowEditorCustomForm' and computes an new custom Field Definition
     * based on a given workitem. The method first looks if the model contains a
     * custom definition. If not the method checks the workitem field
     * txtWorkflowEditorCustomForm which holds the last parsed custom form
     * definition
     * 
     * @return
     * @throws ModelException
     */
    public void computeFieldDefinition(ItemCollection workitem) throws ModelException {
        logger.fine("---> computeFieldDefinition");
        String content = customFormService.updateCustomFieldDefinition(workitem);
        sections = new ArrayList<CustomFormSection>();
        if (!content.isEmpty()) {
            // start parsing....
            logger.finest("......start parsing custom form definition");
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
                Document doc = builder.parse(stream);
                doc.getDocumentElement().normalize();

                // find the root tag
                org.w3c.dom.Element rootElement = doc.getDocumentElement();

                // find imixs-subforms - this tag is optional.
                // if we have subforms we parse each subform tag for form-section tags
                NodeList nSubformList = rootElement.getElementsByTagName("imixs-subform");
                if (nSubformList != null && nSubformList.getLength() > 0) {
                    subforms = new ArrayList<CustomSubForm>();
                    for (int subId = 0; subId < nSubformList.getLength(); subId++) {
                        Element nSubFormElement = (Element) nSubformList.item(subId);
                        String label = nSubFormElement.getAttribute("label");
                        String sReadOnly = nSubFormElement.getAttribute("readonly");
                        boolean bReadOnly = false;
                        if (sReadOnly != null && !sReadOnly.isEmpty()) {
                            bReadOnly = Boolean.parseBoolean(sReadOnly);
                        }
                        CustomSubForm customSubForm = new CustomSubForm("subform-" + (subId + 1), label, bReadOnly);
                        sections = parseSectionList(nSubFormElement, bReadOnly);
                        customSubForm.setSections(sections);
                        subforms.add(customSubForm);
                    }
                } else {
                    // no subform defined - so simply parse all imixs-form-section tags
                    sections = parseSectionList(rootElement, false);
                }
            } catch (ParserConfigurationException | SAXException | IOException e) {
                logger.warning("Unable to parse custom form definition: " + e.getMessage());
                return;
            }

        }

    }

    /**
     * This method overwrite the 'readOnly' status flag for all sections.
     * Note: sections can be embedded in optional subForms.
     * 
     * @param readOnly
     */
    public void setReadOnly(boolean readOnly) {
        if (getSubforms() == null || getSubforms().size() == 0) {
            // only update sections....
            for (CustomFormSection _section : getSections()) {
                _section.setReadonly(readOnly);
                for (CustomFormItem _item : _section.getItems()) {
                    _item.setReadonly(readOnly);
                }
            }
        } else {
            // update all subforms and sections
            for (CustomSubForm _subform : getSubforms()) {
                for (CustomFormSection _section : _subform.getSections()) {
                    _section.setReadonly(readOnly);
                    for (CustomFormItem _item : _section.getItems()) {
                        _item.setReadonly(readOnly);
                    }

                }
            }
        }
    }

    /**
     * This helper method pareses a parent-node for 'imixs-form-section'
     * tags and returns a list of CustomFormSections
     * 
     * @param parentNode - the parent node to be parsed
     * @param readOnly   - if true, all items will become readonly independent from
     *                   their custom settings.
     * @return list of customFormSection elements
     * @throws ModelException
     */
    private List<CustomFormSection> parseSectionList(Element parentNode, boolean readOnly) throws ModelException {
        ArrayList<CustomFormSection> result = new ArrayList<CustomFormSection>();
        boolean defaultReadOnly = false;
        NodeList nSectionList = parentNode.getElementsByTagName("imixs-form-section");
        for (int temp = 0; temp < nSectionList.getLength(); temp++) {
            Node nSectionNode = nSectionList.item(temp);
            logger.finest("parsing section...");
            if (nSectionNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eSectionElement = (Element) nSectionNode;
                if (readOnly == true) {
                    // ignore the readonly attribute
                    defaultReadOnly = true;
                } else {
                    // parse the readonly flag for each item
                    String sReadOnly = eSectionElement.getAttribute("readonly");
                    defaultReadOnly = false;
                    if (sReadOnly != null && !sReadOnly.isEmpty()) {
                        defaultReadOnly = Boolean.parseBoolean(sReadOnly);
                    }
                }
                CustomFormSection customSection = new CustomFormSection(
                        eSectionElement.getAttribute("label"),
                        eSectionElement.getAttribute("columns"),
                        eSectionElement.getAttribute("path"),
                        defaultReadOnly);
                customSection.setItems(findItems(eSectionElement,
                        customSection.getColumns(), defaultReadOnly));
                result.add(customSection);
            }
        }
        return result;
    }

    /**
     * This method parses the item nods with a section element. The param columns
     * defines the span for each item
     * 
     * @param sectionElement
     * @param columns        - default flex layout
     * @return
     * @throws ModelException
     */
    private List<CustomFormItem> findItems(Element sectionElement, String _columns, boolean readOnly)
            throws ModelException {
        List<CustomFormItem> result = new ArrayList<CustomFormItem>();

        // convert columns in flex layout span
        int span = 12; // default;
        if ("2".equals(_columns)) {
            span = 6;
        }
        if ("3".equals(_columns)) {
            span = 4;
        }
        if ("4".equals(_columns)) {
            span = 3;
        }
        if ("6".equals(_columns)) {
            span = 2;
        }

        NodeList itemList = sectionElement.getElementsByTagName("item");
        for (int temp = 0; temp < itemList.getLength(); temp++) {
            Node itemNode = itemList.item(temp);
            logger.finest("......parsing item...");
            if (itemNode.getNodeType() == Node.ELEMENT_NODE) {

                Element itemElement = (Element) itemNode;

                if (itemElement.hasAttribute("span")) {
                    try {
                        span = Integer.parseInt(itemElement.getAttribute("span"));
                    } catch (NumberFormatException e) {
                        // fallback default
                    }
                }

                boolean defaultReadOnly = false;
                if (readOnly == true) {
                    // ignore the readonly attribute
                    defaultReadOnly = true;
                } else {
                    // evaluate the readonly flag depending on the item attribute
                    defaultReadOnly = evaluateBoolean(itemElement.getAttribute("readonly"));
                }
                CustomFormItem customItem = new CustomFormItem(itemElement.getAttribute("name"),
                        itemElement.getAttribute("type"), itemElement.getAttribute("label"),
                        evaluateBoolean(itemElement.getAttribute("required")),
                        defaultReadOnly,
                        evaluateBoolean(itemElement.getAttribute("disabled")), itemElement.getAttribute("options"),
                        itemElement.getAttribute("path"), evaluateBoolean(itemElement.getAttribute("hide")), span);

                result.add(customItem);
            }
        }

        return result;

    }

    /**
     * This method evaluates a Expression to a boolean.
     * <p>
     * An expression can be a EL expression if it starts with '#{' and ends with
     * '}'. In this case the method evaluates the EL expression to boolean.
     * <p>
     * If the expression is a string with 'true' or 'false', then the provided
     * string will be evaluated with Boolean.parseBoolean
     * <p>
     * Example valid values are: 'true', 'false', #{1 eq 1}
     * 
     * @param expression
     * @return
     * @throws ModelException
     */
    private boolean evaluateBoolean(String expression) throws ModelException {
        if (expression == null || expression.isEmpty()) {
            return false;
        }

        // is it a el expression?
        if (expression.startsWith("#{") && expression.endsWith("}")) {

            // test if expression is supported
            if (supportedExpressions.contains(expression)) {
                FacesContext ctx = FacesContext.getCurrentInstance();
                Application app = ctx.getApplication();
                boolean result = app.evaluateExpressionGet(ctx, expression, Boolean.class);
                return result;
            } else {
                throw new ModelException(ModelException.INVALID_MODEL_ENTRY,
                        "The custom-form expression is not allowed: "
                                + expression);
            }

        } else {
            // default simple Bollean parsing....
            return Boolean.parseBoolean(expression);
        }

    }

    /**
     * This helper method loads the epxressions.properties file
     */
    private void loadCustomFormExpressions() {
        try {
            StringBuilder resultStringBuilder = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(getFileFromResourceAsStream("customform.expressions")))) {
                String line;
                while ((line = br.readLine()) != null) {
                    resultStringBuilder.append(line).append("\n");
                }
            }
            supportedExpressions = resultStringBuilder.toString();
        } catch (Exception e) {
            logger.warning("unable to find customform.expressions in current classpath");
            if (logger.isLoggable(Level.FINE)) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Helper method to get a file from the resources folder works everywhere, IDEA,
     * unit test and JAR file.
     * 
     * @see https://mkyong.com/java/java-read-a-file-from-resources-folder/
     * @param fileName
     * @return
     */
    private InputStream getFileFromResourceAsStream(String fileName) {
        // The class loader that loaded the class
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);
        // the stream holding the file content
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return inputStream;
        }
    }
}
