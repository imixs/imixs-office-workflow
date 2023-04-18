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

package org.imixs.workflow.office.config;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Priority;
import jakarta.annotation.Resource;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.ConcurrencyManagement;
import jakarta.ejb.ConcurrencyManagementType;
import jakarta.ejb.EJB;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Singleton;
import jakarta.enterprise.event.Observes;
import jakarta.interceptor.Interceptor;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.DocumentEvent;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.engine.TextEvent;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.exceptions.QueryException;
import org.imixs.workflow.util.XMLParser;

/**
 * The TextBlockService is an EJB handling documents containing textual
 * information. A text-block document is identified by its ID (txtname) and
 * holds a HTML or PlainText information.
 * 
 * A text-block document holds the following items
 * 
 * <ul>
 * <li>txtmode - html/text</li>
 * <li>txtcontent - textual information</li>
 * </ul>
 * 
 * The type of a textBlock document is 'textblock'
 * 
 * A text-block can only be edited by a MANAGER. A text-block has no read
 * restriction.
 * 
 * 
 * 
 * 
 * The TextBlockService ejb is implemented as a sigelton and uses an internal
 * cache to cache config entities.
 * 
 * The Method onEvent is a CDI observer method reacting on events from the type
 * TextEvent. See the WorkflowService.adaptText() method for details.
 * 
 * @author rsoika
 */

@DeclareRoles({ "org.imixs.ACCESSLEVEL.NOACCESS", "org.imixs.ACCESSLEVEL.READERACCESS",
        "org.imixs.ACCESSLEVEL.AUTHORACCESS", "org.imixs.ACCESSLEVEL.EDITORACCESS",
        "org.imixs.ACCESSLEVEL.MANAGERACCESS" })
@RolesAllowed({ "org.imixs.ACCESSLEVEL.NOACCESS", "org.imixs.ACCESSLEVEL.READERACCESS",
        "org.imixs.ACCESSLEVEL.AUTHORACCESS", "org.imixs.ACCESSLEVEL.EDITORACCESS",
        "org.imixs.ACCESSLEVEL.MANAGERACCESS" })
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class TextBlockService {

    int DEFAULT_CACHE_SIZE = 30;

    @EJB
    private DocumentService documentService;

    @EJB
    private WorkflowService workflowService;

    @Resource
    SessionContext ctx;

    private Cache cache = null;

    final String TYPE = "textblock";

    private static Logger logger = Logger.getLogger(TextBlockService.class.getName());

    /**
     * PostContruct event - loads the imixs.properties.
     */
    @PostConstruct
    void init() {
        // initialize cache
        cache = new Cache(DEFAULT_CACHE_SIZE);
    }

    /**
     * This method deletes an existing text-block.
     * 
     * @param aconfig
     * @throws AccessDeniedException
     */
    public void deleteTextBlock(ItemCollection aconfig) throws AccessDeniedException {
        cache.remove(aconfig.getItemValueString("txtName"));
        documentService.remove(aconfig);
    }

    /**
     * This method returns a text-block ItemCollection for a specified name or ID.
     * If no text-block is found for this name the Method creates an empty
     * text-block object. The text-block entity is cached internally and read from
     * the cache
     * 
     * @param name in attribute txtname
     * 
     */
    public ItemCollection loadTextBlock(String name) {
        return this.loadTextBlock(name, false);
    }

    /**
     * This method returns a text-block ItemCollection for a specified name or id.
     * If no text-block is found for this name the Method creates an empty
     * text-block object. The text-block entity is cached internally.
     * 
     * 
     * @param name         in attribute txtname
     * 
     * @param discardCache - indicates if the internal cache should be discarded.
     */
    public ItemCollection loadTextBlock(String name, boolean discardCache) {
        ItemCollection textBlockItemCollection = null;
        // check cache...
        textBlockItemCollection = cache.get(name);
        if (textBlockItemCollection == null || discardCache) {

            // try to load by ID....
            textBlockItemCollection = documentService.load(name);
            if (textBlockItemCollection == null) {
                // not found by ID so lets try to load it by txtname.....
                // load text-block....
                String sQuery = "(type:\"" + TYPE + "\" AND txtname:\"" + name + "\")";
                Collection<ItemCollection> col;
                try {
                    col = documentService.find(sQuery, 1, 0);

                    if (col.size() > 0) {
                        textBlockItemCollection = col.iterator().next();
                    } else {
                        logger.warning("Missing text-block : '" + name + "'");
                    }
                } catch (QueryException e) {
                    logger.warning("getTextBlock - invalid query: " + e.getMessage());
                }

            }

            if (textBlockItemCollection == null) {
                // create default values
                textBlockItemCollection = new ItemCollection();
                textBlockItemCollection.replaceItemValue("type", TYPE);
                textBlockItemCollection.replaceItemValue("txtname", name);
            }
            cache.put(name, textBlockItemCollection);
        }
        return textBlockItemCollection;
    }

    /**
     * SavesO the text-block entity
     * 
     * @return
     * @throws AccessDeniedException
     */
    public ItemCollection save(ItemCollection textBlockItemCollection) throws AccessDeniedException {
        if (textBlockItemCollection == null) {
            return textBlockItemCollection;
        }
        // update write and read access
        textBlockItemCollection.replaceItemValue("type", TYPE);
        // update write and read access
        textBlockItemCollection.replaceItemValue("$writeAccess", "org.imixs.ACCESSLEVEL.MANAGERACCESS");
        textBlockItemCollection.replaceItemValue("$readAccess", "");
        // trim txtname
        textBlockItemCollection.replaceItemValue("txtname",
                textBlockItemCollection.getItemValueString("txtName").trim());
        // editor...
        textBlockItemCollection.replaceItemValue("$Editor", ctx.getCallerPrincipal().getName().toString());
        textBlockItemCollection.replaceItemValue("namcurrentEditor", ctx.getCallerPrincipal().getName().toString());

        // save entity
        textBlockItemCollection = documentService.save(textBlockItemCollection);

        cache.put(textBlockItemCollection.getItemValueString("txtName"), textBlockItemCollection);

        return textBlockItemCollection;
    }

    /**
     * This method reacts on CDI events of the type TextEvent and parses a string
     * for xml tag <textblock>. Those tags will be replaced with the corresponding
     * system property value.
     * <p>
     * The priority of the CDI event is set to (APPLICATION+10) to ensure that the
     * textblock adapter is triggered after the TextItemValueAdapter
     * 
     * 
     */
    public void onTextEvent(@Observes @Priority(Interceptor.Priority.APPLICATION + 600) TextEvent event) {
        String text = event.getText();

        // lower case <textBlock> into <textblock>
        if (text.contains("<textBlock") || text.contains("</textBlock>")) {
            logger.warning("Deprecated <textBlock> tag should be lowercase <textblock> !");
            text = text.replace("<textBlock", "<textblock");
            text = text.replace("</textBlock>", "</textblock>");
        }

        List<String> tagList = XMLParser.findTags(text, "textblock");
        logger.finest(tagList.size() + " tags found");
        // test if a <value> tag exists...
        for (String tag : tagList) {

            // read the textblock Value
            String sTextBlockKey = XMLParser.findTagValue(tag, "textblock");

            ItemCollection textBlockItemCollection = loadTextBlock(sTextBlockKey);
            if (textBlockItemCollection != null) {
                String sValue = "";
                // is the text block in mode 'FILE'
                if ("FILE".equals(textBlockItemCollection.getItemValueString("txtmode"))) {
                    // take the 1st file name.....
                    List<String> files = textBlockItemCollection.getFileNames();
                    if (files != null && files.size() > 0) {
                        sValue = files.get(0);
                    }
                    if (files.size() > 1) {
                        logger.warning("textblock '" + sTextBlockKey + "' contains more than one file!");
                    }
                    if (sValue.trim().isEmpty()) {
                        logger.warning("textblock '" + sTextBlockKey + "' type FILE contains no file!");
                        sValue = " - missing file - ";
                    }
                } else {
                    // default - read content mode TEXT|HTML
                    sValue = textBlockItemCollection.getItemValueString("txtcontent");
                }
                // now replace the tag with the result string
                int iStartPos = text.indexOf(tag);
                int iEndPos = text.indexOf(tag) + tag.length();

                // adapt the textblock value !
                try {
                    sValue = workflowService.adaptText(sValue, event.getDocument());
                } catch (PluginException e) {
                    logger.warning("Unable to adapt text within textblock '" + sTextBlockKey + "' : " + e.getMessage());
                }

                // now replace the tag with the result string
                text = text.substring(0, iStartPos) + sValue + text.substring(iEndPos);

            } else {
                logger.warning("text-block '" + sTextBlockKey + "' is not defined!");
            }
        }

        event.setText(text);

    }

    /**
     * This method refreshes the cache if a textblock was saved.
     * 
     * @param event
     */
    public void onDocumentEvent(@Observes DocumentEvent event) {

        if (TYPE.equals(event.getDocument().getType()) && (event.getEventType() == DocumentEvent.ON_DOCUMENT_SAVE
                || event.getEventType() == DocumentEvent.ON_DOCUMENT_DELETE)) {
            cache = new Cache(DEFAULT_CACHE_SIZE);
        }

    }

    /**
     * Cache implementation to hold config entities
     * 
     * @author rsoika
     * 
     */
    class Cache extends ConcurrentHashMap<String, ItemCollection> implements Serializable {
        private static final long serialVersionUID = 1L;
        private final int capacity;

        public Cache(int capacity) {
            super(capacity + 1, 1.1f);
            this.capacity = capacity;
        }

        protected boolean removeEldestEntry(Entry<String, ItemCollection> eldest) {
            return size() > capacity;
        }
    }

}
