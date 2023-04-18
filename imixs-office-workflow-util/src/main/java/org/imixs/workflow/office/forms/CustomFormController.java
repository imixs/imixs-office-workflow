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

import jakarta.ejb.EJB;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.faces.application.Application;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.Model;
import org.imixs.workflow.engine.ModelService;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.faces.data.WorkflowEvent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The CustomFormController computes a set of fields based on a data object
 * provided by the model.
 * 
 * 
 * @author rsoika
 * 
 */
@Named
@ConversationScoped
public class CustomFormController implements Serializable {

    @EJB
    ModelService modelService;

    private static final long serialVersionUID = 1L;

    private static Logger logger = Logger.getLogger(CustomFormController.class.getName());

    private List<CustomFormSection> sections;

    private String supportedExpressions = "";

    public CustomFormController() {
        super();
        loadCustomFormExpressions();
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
        if (WorkflowEvent.WORKITEM_CHANGED == eventType || WorkflowEvent.WORKITEM_CREATED == eventType
                || WorkflowEvent.WORKITEM_AFTER_PROCESS == eventType) {
            // parse form definition
            computeFieldDefinition(workflowEvent.getWorkitem());
        }

    }

    /**
     * Computes an new custom Field Definition based on a given workitem. The method
     * first looks if the model contains a custom definition. If not the method
     * checks the workitem field txtWorkflowEditorCustomForm which holds the last
     * parsed custom form definition
     * 
     * @return
     * @throws ModelException
     */
    public void computeFieldDefinition(ItemCollection workitem) throws ModelException {
        sections = new ArrayList<CustomFormSection>();
        String content = fetchFormDefinitionFromModel(workitem);
        if (content.isEmpty()) {
            // lets see if we already have a custom form definition
            content = workitem.getItemValueString("txtWorkflowEditorCustomForm");
        }

        if (!content.isEmpty()) {
            // start parsing....
            logger.finest("......start parsing custom form definition");
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
                Document doc = builder.parse(stream);
                doc.getDocumentElement().normalize();

                NodeList nList = doc.getElementsByTagName("imixs-form-section");
                for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node nNode = nList.item(temp);
                    logger.finest("parsing section...");
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;

                        String sReadOnly=eElement.getAttribute("readonly");
                        boolean bReadOnly=false;
                        if (sReadOnly!=null && !sReadOnly.isEmpty()) {
                            bReadOnly=Boolean.parseBoolean(sReadOnly);
                        }
                        CustomFormSection customSection = new CustomFormSection(eElement.getAttribute("label"),
                                eElement.getAttribute("columns"), eElement.getAttribute("path"),bReadOnly);
                        customSection.setItems(findItems(eElement));
                        sections.add(customSection);
                    }
                }
            } catch (ParserConfigurationException | SAXException | IOException e) {
                logger.warning("Unable to parse custom form definition: " + e.getMessage());
                return;

            }
            // store the new content
            workitem.replaceItemValue("txtWorkflowEditorCustomForm", content);
        }

    }

    /**
     * read the form definition from a dataObject and search for a dataobject with a
     * imixs-form tag. If not matching dataobject is defined then return an empty
     * string.
     * 
     * @param workitem
     * @return
     */
    @SuppressWarnings("unchecked")
    private String fetchFormDefinitionFromModel(ItemCollection workitem) {
        Model model;
        ItemCollection task;
        try {
            model = modelService.getModelByWorkitem(workitem);
            task = model.getTask(workitem.getTaskID());
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

    /**
     * This method parses the item nods with a section element
     * 
     * @param sectionElement
     * @return
     * @throws ModelException 
     */
    private List<CustomFormItem> findItems(Element sectionElement) throws ModelException {
        List<CustomFormItem> result = new ArrayList<CustomFormItem>();
        NodeList itemList = sectionElement.getElementsByTagName("item");
        for (int temp = 0; temp < itemList.getLength(); temp++) {
            Node itemNode = itemList.item(temp);
            logger.finest("......parsing item...");
            if (itemNode.getNodeType() == Node.ELEMENT_NODE) {

                Element itemElement = (Element) itemNode;
                CustomFormItem customItem = new CustomFormItem(itemElement.getAttribute("name"),
                        itemElement.getAttribute("type"), itemElement.getAttribute("label"),
                        evaluateBoolean(itemElement.getAttribute("required")),
                        evaluateBoolean(itemElement.getAttribute("readonly")), itemElement.getAttribute("options"),
                        itemElement.getAttribute("path"), evaluateBoolean(itemElement.getAttribute("hide")));

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
                throw new ModelException(ModelException.INVALID_MODEL_ENTRY, "The custom-form expression is not allowed: "
            + expression  );
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
