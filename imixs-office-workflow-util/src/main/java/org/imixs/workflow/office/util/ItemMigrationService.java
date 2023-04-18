package org.imixs.workflow.office.util;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.enterprise.event.Observes;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.DocumentEvent;

/**
 * This EJB provides reacts on Document Load/SAve events and automatically
 * migrates deprecated field items.
 * <p>
 * e.g. namTeam -> space.team
 * 
 * 
 * @version 1.0
 * @author rsoika
 * 
 */
@Stateless
@LocalBean
public class ItemMigrationService {

    /**
     * Reacts on ON_DOCUMENT_SAVE / ON_DOCUMENT_LOAD for the following document
     * types:
     * <p>
     * <ul>
     * <li>space</li>
     * <li>process</li>
     * </ul>
     * 
     * @see DocumentEvent
     * @param documentEvent
     */
    public void onDocumentEvent(@Observes DocumentEvent documentEvent) {

        String type = documentEvent.getDocument().getType();

        if (type.startsWith("space")) {
            if (documentEvent.getEventType() == DocumentEvent.ON_DOCUMENT_LOAD) {
                migrate(documentEvent.getDocument(), "nammanager", "space.manager");
                migrate(documentEvent.getDocument(), "namteam", "space.team");
                migrate(documentEvent.getDocument(), "namassist", "space.assist");
                migrate(documentEvent.getDocument(), "txtname", "name");
                migrate(documentEvent.getDocument(), "txtspacename", "space.name");
                migrate(documentEvent.getDocument(), "txtspaceref", "space.ref");
            }

        }

        if (type.startsWith("process")) {
            if (documentEvent.getEventType() == DocumentEvent.ON_DOCUMENT_LOAD) {
                migrate(documentEvent.getDocument(), "nammanager", "process.manager");
                migrate(documentEvent.getDocument(), "namteam", "process.team");
                migrate(documentEvent.getDocument(), "namassist", "process.assist");
                migrate(documentEvent.getDocument(), "txtname", "name");
                // issue #351 - migrate(documentEvent.getDocument(), "txtname", "process.name");
                migrate(documentEvent.getDocument(), "txtprocessref", "process.ref");
            }

        }

    }

    /**
     * Migrates an old item name into a new item name.
     * <p>
     * The method verifies if the new item already exists. If so - no migration will
     * be performed
     * 
     * @param document
     * @param oldItem
     * @param newItem
     */
    private void migrate(ItemCollection document, String oldItem, String newItem) {
        if (document.hasItem(newItem)) {
            // no op
            return;
        }

        // migrate
        document.replaceItemValue(newItem, document.getItemValue(oldItem));

    }

}
