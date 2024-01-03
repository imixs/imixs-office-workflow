package org.imixs.workflow.office.gdpr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.WorkflowKernel;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.exceptions.PluginException;

import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.annotation.security.RunAs;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.mail.internet.AddressException;

/**
 * This GDPRAnonymiseService provides methods to delete or anonymise items in a
 * workitem.
 * 
 * @author rsoika
 * @version 2.0
 * 
 */
@DeclareRoles({ "org.imixs.ACCESSLEVEL.MANAGERACCESS" })
@RolesAllowed({ "org.imixs.ACCESSLEVEL.MANAGERACCESS" })
@Singleton
@RunAs("org.imixs.ACCESSLEVEL.MANAGERACCESS")
public class GDPRAnonymiseService {
	private static Logger logger = Logger.getLogger(GDPRAnonymiseService.class.getName());
	public static String PROCESSING_ERROR = "PROCESSING_ERROR";

	@Inject
	DocumentService documentService;

	/**
	 * The method deletes a list of items. The items can be defined by name or in a
	 * regular expression
	 * 
	 * @return
	 * @throws PluginException
	 * @throws AddressException
	 */
	public ItemCollection deleteItems(ItemCollection workitem, String itemDescription)
			throws PluginException {
		logger.finest("......delete items for '" + workitem.getUniqueID() + "'");
		// delete
		List<String> itemList = resolveItemDescription(workitem, itemDescription);
		if (itemList != null && !itemList.isEmpty()) {
			for (String itemName : itemList) {
				logger.finest("......delete item: " + itemName);
				workitem.removeItem(itemName);
			}
		}
		return workitem;
	}

	/**
	 * The method anonymises a list of items. The items can be defined by name or in
	 * a regular expression
	 * 
	 * @return
	 * @throws PluginException
	 * @throws AddressException
	 */
	public ItemCollection anonimiseItems(ItemCollection workitem, String itemDescription, String placeholder)
			throws PluginException {
		logger.finest("......anonymizing items for '" + workitem.getUniqueID() + "'. Placeholder='"
				+ placeholder + "'");
		// anonymize
		List<String> itemList = resolveItemDescription(workitem, itemDescription);
		if (itemList != null && !itemList.isEmpty()) {
			for (String itemName : itemList) {
				logger.finest("......anonymize item: " + itemName);
				workitem.replaceItemValue(itemName, placeholder);
			}
		}
		return workitem;
	}

	/**
	 * This helper method resolves a ItemDescripton and returns all matching Items
	 * in the given Workitem.
	 * A item can be the name or a regular expression.
	 * 
	 * @param workitem
	 * @param itemDescription
	 * @return
	 */
	public List<String> resolveItemDescription(ItemCollection workitem, String itemDescription) {
		List<String> result = new ArrayList<>();
		StringTokenizer st = new StringTokenizer(itemDescription, ",");
		while (st.hasMoreTokens()) {
			String key = st.nextToken().trim();
			// test if key is a reg ex
			if (key.startsWith("(") && key.endsWith(")")) {
				Pattern itemPattern = Pattern.compile(key);
				Map<String, List<Object>> map = workitem.getAllItems();
				for (String itemName : map.keySet()) {
					if (itemPattern.matcher(itemName).find()) {
						result.add(itemName);
					}
				}
			} else {
				// key is the itemName
				result.add(key);
			}
		}

		// ignore WorkflowKernel.UNIQUEID
		while (result.contains(WorkflowKernel.UNIQUEID)) {
			result.remove(WorkflowKernel.UNIQUEID);
		}

		return result;
	}

	/**
	 * Saves as manager
	 * 
	 * @param workitem
	 */
	public void save(ItemCollection workitem) {
		documentService.save(workitem);
	}
}