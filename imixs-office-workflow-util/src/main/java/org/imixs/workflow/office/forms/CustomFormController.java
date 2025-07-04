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
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.ModelManager;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.faces.data.WorkflowEvent;
import org.openbpmn.bpmn.BPMNModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import jakarta.annotation.PostConstruct;
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
    private static Logger logger = Logger.getLogger(CustomFormController.class.getName());

    public static final String ITEM_CUSTOM_FORM = "txtWorkflowEditorCustomForm";

    private List<CustomSubForm> subforms = null;
    private ModelManager modelManager = null;
    private List<CustomFormSection> sections;
    private String supportedExpressions = "";

    @Inject
    WorkflowService workflowService;

    public CustomFormController() {
        super();
    }

    /**
     * Initializes the ModelManger
     */
    @PostConstruct
    public void init() {
        modelManager = new ModelManager(workflowService);
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
        if (WorkflowEvent.WORKITEM_CHANGED == eventType
                || WorkflowEvent.WORKITEM_CREATED == eventType
                || WorkflowEvent.WORKITEM_AFTER_PROCESS == eventType) {
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
        String content = updateCustomFieldDefinition(workitem);
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

                // find imixs-subform - this tag is optional.
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
                        sections = parseSectionList(nSubFormElement, bReadOnly, workitem);
                        customSubForm.setSections(sections);
                        subforms.add(customSubForm);
                    }
                } else {
                    // no subform defined - so simply parse all imixs-form-section tags
                    sections = parseSectionList(rootElement, false, workitem);
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
    private List<CustomFormSection> parseSectionList(Element parentNode, boolean readOnly, ItemCollection workitem)
            throws ModelException {
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
                        customSection.getColumns(), defaultReadOnly, workitem));
                result.add(customSection);
            }
        }
        return result;
    }

    /**
     * This method parses the item nodes within a section element. The param columns
     * defines the span for each item
     * 
     * @param sectionElement
     * @param columns        - default flex layout
     * @return
     * @throws ModelException
     */
    private List<CustomFormItem> findItems(Element sectionElement, String _columns, boolean readOnly,
            ItemCollection workitem)
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

                // test if we have a default value
                String defaultValue = itemElement.getTextContent();
                if (defaultValue != null && !defaultValue.isEmpty()) {
                    // extract raw values
                    String itemName = itemElement.getAttribute("name");
                    try {
                        defaultValue = getInnerXMLContent(itemElement);
                        if (!workitem.hasItem(itemName)) {
                            // adapt text....
                            if (defaultValue.contains("$created")) {
                                // special case - we resolve the manually here!
                                workitem.setItemValue(itemElement.getAttribute("name"), new Date());
                            } else {
                                // use adaptText method in all other cases
                                defaultValue = workflowService.adaptText(defaultValue, workitem);
                                workitem.setItemValue(itemElement.getAttribute("name"), defaultValue);
                            }
                        }
                    } catch (PluginException e) {
                        logger.warning(
                                "failed to adapt default value for item " + itemName + " : " + e.getMessage());
                    }
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
     * Helper method to extract the content of a xml tag including child tags
     **/
    public static String getInnerXMLContent(Element element) {
        Transformer transformer;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty("omit-xml-declaration", "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(element), new StreamResult(writer));
            String xmlString = writer.toString().trim();
            Pattern pattern = Pattern.compile(
                    "<" + element.getNodeName() + "[^>]*>(.*?)</" + element.getNodeName() + ">", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(xmlString);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (TransformerException e) {
            // no op
        }
        // default behavior
        return element.getTextContent();
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

    /**
     * This method updates the custom Field Definition based on a given workitem.
     * The method first looks if the task associated with the workitem contains a
     * bpmn:DataObject containing a custom definition.
     * The result is stored into the item <code>txtWorkflowEditorCustomForm</code>.
     * <p>
     * In case the model does not provide a custom Field Definition but the workitem
     * has stored one the method returns the existing one and did not update the
     * item <code>txtWorkflowEditorCustomForm</code>.
     * 
     * @return
     * @throws ModelException
     */
    public String updateCustomFieldDefinition(ItemCollection workitem)
            throws ModelException {
        String content = fetchFormDefinitionFromModel(workitem);
        if (content.isEmpty()) {
            // take the existing one to be returned...
            content = workitem.getItemValueString(ITEM_CUSTOM_FORM);
        } else {
            workitem.replaceItemValue(ITEM_CUSTOM_FORM, content);
        }
        return content;
    }

    /**
     * Helper method that reads a form definition from an optional
     * <code>bpmn:DataObject</code> associated with the current task element.
     * A <code>bpmn:DataObject</code> must contain a `form-tag` containing the form
     * definition.
     * If not matching <code>bpmn:DataObject</code> is defined the method returns an
     * empty
     * string.
     * 
     * @param workitem
     * @return
     */
    @SuppressWarnings("unchecked")
    private String fetchFormDefinitionFromModel(ItemCollection workitem) {
        ItemCollection task;
        try {
            BPMNModel model = modelManager.getModelByWorkitem(workitem);
            task = modelManager.loadTask(workitem, model);

        } catch (ModelException e) {
            logger.warning("unable to parse data object in model: " + e.getMessage());
            return "";
        }

        List<List<String>> dataObjects = task.getItemValue("dataObjects");
        for (List<String> dataObject : dataObjects) {
            // there can be more than one dataOjects be attached.
            // We need the one with the tag <imixs-form>
            String templateName = dataObject.get(0);
            String content = dataObject.get(1);
            // we expect that the content contains at least one occurrence of <imixs-form>
            if (content.contains("<imixs-form>")) {
                logger.finest("......DataObject name=" + templateName);
                logger.finest("......DataObject content=" + content);
                return content;
            } else {
                // seems not to be a imixs-form definition!
            }
        }
        // nothing found!
        return "";
    }
}
