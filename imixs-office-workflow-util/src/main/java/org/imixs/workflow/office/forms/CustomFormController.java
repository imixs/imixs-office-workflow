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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Named;
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

    public CustomFormController() {
        super();
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
    public void onWorkflowEvent(@Observes WorkflowEvent workflowEvent) {
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
    public void computeFieldDefinition(ItemCollection workitem) {
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
                        
                        CustomFormSection customSection = new CustomFormSection(eElement.getAttribute("label"),
                                eElement.getAttribute("columns"),eElement.getAttribute("path"));
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
     */
    private List<CustomFormItem> findItems(Element sectionElement) {
        List<CustomFormItem> result = new ArrayList<CustomFormItem>();
        NodeList itemList = sectionElement.getElementsByTagName("item");
        for (int temp = 0; temp < itemList.getLength(); temp++) {
            Node itemNode = itemList.item(temp);
            logger.finest("......parsing item...");
            if (itemNode.getNodeType() == Node.ELEMENT_NODE) {

                Element itemElement = (Element) itemNode;
                CustomFormItem customItem = new CustomFormItem(itemElement.getAttribute("name"),
                        itemElement.getAttribute("type"), itemElement.getAttribute("label"),
                        Boolean.parseBoolean(itemElement.getAttribute("required")),
                        Boolean.parseBoolean(itemElement.getAttribute("readonly")),
                        itemElement.getAttribute("options"), itemElement.getAttribute("path"));

                result.add(customItem);
            }
        }

        return result;

    }
}
