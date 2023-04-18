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

package org.imixs.workflow.office.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.annotation.security.RunAs;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.WorkflowKernel;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.office.config.ConfigService;
import org.imixs.workflow.util.XMLParser;

/**
 * The SequcneceService is a singleton EJB which handles continuous
 * sequenceNumbers for a workitem separated for each workflowGroup.
 * <p>
 * The sequence numbers are stored in the item 'sequencenumbers' of the
 * configuration entity with the name "BASIC" in in the following format
 * <p>
 * <code>[GROUP]=123</code>
 * <p>
 * <strong>Optimistic Locking Problem</strong>
 * <p>
 * In earlier versions, the method runs in a OptimisticLockException in case
 * that multiple processes run in parallel. To fix this the service is changed
 * into a singleton. See issue #290.
 * <p>
 *
 * @author rsoika
 * 
 */

@DeclareRoles({ "org.imixs.ACCESSLEVEL.NOACCESS", "org.imixs.ACCESSLEVEL.READERACCESS",
        "org.imixs.ACCESSLEVEL.AUTHORACCESS", "org.imixs.ACCESSLEVEL.EDITORACCESS",
        "org.imixs.ACCESSLEVEL.MANAGERACCESS" })
@RolesAllowed({ "org.imixs.ACCESSLEVEL.NOACCESS", "org.imixs.ACCESSLEVEL.READERACCESS",
        "org.imixs.ACCESSLEVEL.AUTHORACCESS", "org.imixs.ACCESSLEVEL.EDITORACCESS",
        "org.imixs.ACCESSLEVEL.MANAGERACCESS" })
@Singleton
@RunAs("org.imixs.ACCESSLEVEL.MANAGERACCESS")
public class SequenceService {

    public static final String ITEM_SEQUENCENUMBER = "sequencenumber";
    public static final String ITEM_SEQUENCENUMBER_DEPRECATED = "numsequencenumber";

    private static Logger logger = Logger.getLogger(SequenceService.class.getName());

    @EJB
    ConfigService configService;

    @EJB
    DocumentService documentService;

    /**
     * This method computes the sequence number based on a configuration entity with
     * the name "BASIC". The configuration provides a property 'sequencenumbers'
     * with the current number range for each workflowGroup. If a Workitem have a
     * WorkflowGroup with no corresponding entry the method will not compute a new
     * number.
     * <p>
     * This method loads and updates the configuration entity in a new transaction.
     * In combination with the
     * 
     * @Singleton pattern a conflict of multipl running processing steps is no
     *            longer possible. See issue #290.
     * 
     * @throws InvalidWorkitemException
     * @throws AccessDeniedException
     */
    @TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
    public void computeSequenceNumber(ItemCollection documentContext) throws AccessDeniedException {

        // if the worktitem already have a sequence number than skip!
        if (hasSequenceNumber(documentContext)) {
            return;
        }

        ItemCollection configItemCollection = configService.loadConfiguration("BASIC", true);
        if (configItemCollection != null) {
            // read configuration and test if a corresponding configuration
            // exists
            String sWorkflowGroup = documentContext.getItemValueString(WorkflowKernel.WORKFLOWGROUP);
            @SuppressWarnings("unchecked")
            List<String> vNumbers = configItemCollection.getItemValue("sequencenumbers");

            // find a matching identifier
            String groupIdentifier = null;
            String generalIdentifier = null;
            int identifierPosition = -1;
            // test if we have a group identifier...
            for (String aIdentifier : vNumbers) {
                // test for group identifier...
                if (aIdentifier.startsWith(sWorkflowGroup + "=")) {
                    groupIdentifier = aIdentifier;
                }
                // test for general identifier...
                if (aIdentifier.startsWith("[GENERAL]=")) {
                    generalIdentifier = aIdentifier;
                }
            }

            // if we did not find a group identifier we choose the GroupIdentifier if
            // available...
            if (groupIdentifier == null && generalIdentifier != null) {
                // select the general identifier...
                groupIdentifier = generalIdentifier;
                sWorkflowGroup = "[GENERAL]";
            }

            // did we found an identifier?
            if (groupIdentifier != null) {
                // compute the next number....
                SequenceNumber seqn = new SequenceNumber(groupIdentifier.substring(groupIdentifier.indexOf('=') + 1));

                documentContext.replaceItemValue(ITEM_SEQUENCENUMBER, seqn.nextSequenceNumber);
                // support deprecated item name
                documentContext.replaceItemValue(ITEM_SEQUENCENUMBER_DEPRECATED, seqn.nextSequenceNumber);
                
                
                // update identifier....
                for (int i = 0; i < vNumbers.size(); i++) {
                    if (vNumbers.get(i).startsWith(sWorkflowGroup + "=")) {
                        identifierPosition = i;
                        break;
                    }
                }
                if (identifierPosition > -1) {
                    vNumbers.set(identifierPosition, sWorkflowGroup + "=" + seqn.nextDev);
                    configItemCollection.replaceItemValue("sequencenumbers", vNumbers);
                    // do not use documentService here - cache need to be
                    // updated!
                    configService.save(configItemCollection);
                }
            } else {
                // to avoid problems with incorrect data values we remove the
                // property numsequencenumber in this case
                documentContext.removeItem(ITEM_SEQUENCENUMBER);
            }

        } else {
            logger.warning("No BASIC configuration found!");
        }

    }

    /**
     * This method verifies if a sequence number already exists.
     * <p>
     * The method also migrate the old item name 'numsequencenumber' into the new
     * item name 'sequencenumber'
     * 
     * @param documentContext
     */
    public boolean hasSequenceNumber(ItemCollection documentContext) {
        // if the worktitem already have a sequence number than skip!
        if (documentContext.hasItem(ITEM_SEQUENCENUMBER)
                && !documentContext.getItemValueString(ITEM_SEQUENCENUMBER).isEmpty()) {
            // support old item name if not available (backward compatibility)
            if (!documentContext.hasItem(ITEM_SEQUENCENUMBER_DEPRECATED)) {
                documentContext.replaceItemValue(ITEM_SEQUENCENUMBER_DEPRECATED,
                        documentContext.getItemValue(ITEM_SEQUENCENUMBER));
            }
            return true;
        }

        // test for deprecated item name
        if (documentContext.hasItem(ITEM_SEQUENCENUMBER_DEPRECATED)
                && !documentContext.getItemValueString(ITEM_SEQUENCENUMBER_DEPRECATED).isEmpty()) {
            documentContext.replaceItemValue(ITEM_SEQUENCENUMBER,
                    documentContext.getItemValue(ITEM_SEQUENCENUMBER_DEPRECATED));
            return true;
        }
        // no sequencenumer defined!
        return false;
    }
  
    /**
     * A SequenceNumber is a internal object separating a fixed part form a number
     * part. It computes the next sequence number as also its new definition
     * <p>
     * e.g. R<YEAR>0001
     * 
     * @author rsoika
     *
     */
    public class SequenceNumber {

        private String digit = "";
        private String nextDigit = "";
        private String prafix = "";
        private String nextSequenceNumber = "";
        private String nextDev = "";

        public SequenceNumber(String def) {
            super();
            // find the number part (we expect the number at the end
            for (int i = def.length() - 1; i >= 0; i--) {
                if (Character.isDigit(def.charAt(i))) {
                    digit = def.charAt(i) + digit;
                } else {
                	// no more number digits (see also issue #501)
                	break;
                }
            }
            prafix = def.substring(0, def.length() - digit.length());

            // now we compute the next absolute number
            long l = Long.parseLong(digit);
            nextDigit = "" + (l + 1);
            // fill leading zeros..
            while (nextDigit.length() < digit.length()) {
                nextDigit = "0" + nextDigit;
            }

            nextDev = prafix + nextDigit;

            // compute next sequecne number
            nextSequenceNumber = def;
            // replace <date>...</date>
            List<String> dateTags = XMLParser.findTags(nextSequenceNumber, "date");
            for (String tag : dateTags) {
                // extract the value with the formating information
                String pattern = XMLParser.findTagValue(tag, "date");
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                String dateValue = simpleDateFormat.format(new Date());
                nextSequenceNumber = nextSequenceNumber.replace(tag, dateValue);
            }

        }

        public String getDigit() {
            return digit;
        }

        public String getNextDigit() {
            return nextDigit;
        }

        public String getPrafix() {
            return prafix;
        }

        public String getNextSequenceNumber() {
            return nextSequenceNumber;
        }

        public String getNextDev() {
            return nextDev;
        }

    }

}
