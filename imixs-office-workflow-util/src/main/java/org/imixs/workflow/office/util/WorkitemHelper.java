package org.imixs.workflow.office.util;

import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.imixs.workflow.ItemCollection;

/**
 * The WorkitemHelper provides methods to clone, compare and sort workitems.
 * 
 * @author rsoika
 * 
 */
public class WorkitemHelper {
	private static Logger logger = Logger.getLogger(WorkitemHelper.class.getName());

	/**
	 * This method clones the given workItem with a minimum of attributes.
	 * 
	 * @param aWorkitem
	 * @return
	 */
	public static ItemCollection clone(ItemCollection aWorkitem) {
		ItemCollection clone = new ItemCollection();

		// clone the standard WorkItem properties
		clone.replaceItemValue("Type", aWorkitem.getItemValue("Type"));
		clone.replaceItemValue("$UniqueID", aWorkitem.getItemValue("$UniqueID"));
		clone.replaceItemValue("$UniqueIDRef", aWorkitem.getItemValue("$UniqueIDRef"));
		clone.replaceItemValue("$ModelVersion", aWorkitem.getItemValue("$ModelVersion"));
		clone.replaceItemValue("$ProcessID", aWorkitem.getItemValue("$ProcessID"));
		clone.replaceItemValue("$Created", aWorkitem.getItemValue("$Created"));
		clone.replaceItemValue("$Modified", aWorkitem.getItemValue("$Modified"));
		clone.replaceItemValue("$isAuthor", aWorkitem.getItemValue("$isAuthor"));
		clone.replaceItemValue("$creator", aWorkitem.getItemValue("$creator"));
		clone.replaceItemValue("$editor", aWorkitem.getItemValue("$editor"));
		
		clone.replaceItemValue("$TaskID", aWorkitem.getItemValue("$TaskID"));
		clone.replaceItemValue("$EventID", aWorkitem.getItemValue("$EventID"));
		clone.replaceItemValue("$workflowGroup", aWorkitem.getItemValue("$workflowGroup"));
		clone.replaceItemValue("$workflowStatus", aWorkitem.getItemValue("$workflowStatus"));
		clone.replaceItemValue("$lastTask", aWorkitem.getItemValue("$lastTask"));
		clone.replaceItemValue("$lastEvent", aWorkitem.getItemValue("$lastEvent"));
		clone.replaceItemValue("$lastEventDate", aWorkitem.getItemValue("$lastEventDate"));
		clone.replaceItemValue("$eventLog", aWorkitem.getItemValue("$eventLog"));
		clone.replaceItemValue("$lasteditor", aWorkitem.getItemValue("$lasteditor"));


		clone.replaceItemValue("txtName", aWorkitem.getItemValue("txtName"));

        clone.replaceItemValue("$WorkflowStatus", aWorkitem.getItemValue("$WorkflowStatus"));
        clone.replaceItemValue("$WorkflowGroup", aWorkitem.getItemValue("$WorkflowGroup"));
		clone.replaceItemValue("namCreator", aWorkitem.getItemValue("namCreator"));
		clone.replaceItemValue("namCurrentEditor", aWorkitem.getItemValue("namCurrentEditor"));
		clone.replaceItemValue("$Owner", aWorkitem.getItemValue("$Owner"));
		clone.replaceItemValue("namOwner", aWorkitem.getItemValue("namOwner"));
		clone.replaceItemValue("namTeam", aWorkitem.getItemValue("namTeam"));
		clone.replaceItemValue("namManager", aWorkitem.getItemValue("namManager"));
		clone.replaceItemValue("namassist", aWorkitem.getItemValue("namassist"));
		
        // deprecated fields
        clone.replaceItemValue("txtWorkflowStatus", aWorkitem.getItemValue("txtWorkflowStatus"));
        clone.replaceItemValue("txtWorkflowGroup", aWorkitem.getItemValue("txtWorkflowGroup"));
		if (aWorkitem.getType().startsWith("space")) {
		    cloneByPraefix("space",aWorkitem,clone);
		}
		if (aWorkitem.getType().startsWith("process")) {
            cloneByPraefix("process",aWorkitem,clone);
        }

		clone.replaceItemValue("$workflowsummary", aWorkitem.getItemValue("$workflowsummary"));
		clone.replaceItemValue("$WorkflowAbstract", aWorkitem.getItemValue("$WorkflowAbstract"));
		clone.replaceItemValue("txtWorkflowSummary", aWorkitem.getItemValue("txtWorkflowSummary"));
		clone.replaceItemValue("txtWorkflowAbstract", aWorkitem.getItemValue("txtWorkflowAbstract"));
		clone.replaceItemValue("txtWorkflowImageURL", aWorkitem.getItemValue("txtWorkflowImageURL"));

		// clone the marty WorkItem properties....
		if (aWorkitem.hasItem("txtName"))
			clone.replaceItemValue("txtName", aWorkitem.getItemValue("txtName"));
        if (aWorkitem.hasItem("Name"))
            clone.replaceItemValue("Name", aWorkitem.getItemValue("Name"));

		if (aWorkitem.hasItem("txtProcessName"))
			clone.replaceItemValue("txtProcessName", aWorkitem.getItemValue("txtProcessName"));
		if (aWorkitem.hasItem("txtSpaceName"))
			clone.replaceItemValue("txtSpaceName", aWorkitem.getItemValue("txtSpaceName"));
		if (aWorkitem.hasItem("datdate"))
			clone.replaceItemValue("datdate", aWorkitem.getItemValue("datdate"));

		if (aWorkitem.hasItem("datFrom"))
			clone.replaceItemValue("datFrom", aWorkitem.getItemValue("datFrom"));

		if (aWorkitem.hasItem("datTo"))
			clone.replaceItemValue("datTo", aWorkitem.getItemValue("datTo"));

		if (aWorkitem.hasItem("numsequencenumber"))
			clone.replaceItemValue("numsequencenumber", aWorkitem.getItemValue("numsequencenumber"));

		return clone;

	}
	
	
	
	/**
	 * Clones all items by a given praefix
	 * @param string
	 * @param aWorkitem
	 * @param clone
	 */
	private static void cloneByPraefix(String praefix, ItemCollection aWorkitem, ItemCollection clone) {
	    List<String> itemNames = aWorkitem.getItemNames();
	    String itempraefix=praefix+".";
	    for (String itemName: itemNames) {
	        if (itemName.startsWith(itempraefix)) {
	            clone.replaceItemValue(itemName, aWorkitem.getItemValue(itemName));
	        }
	    }
        
    }




	/**
	 * This method tests if a given WorkItem matches a filter expression. The
	 * expression is expected in a column separated list of reg expressions for
	 * Multiple properties. - e.g.:
	 * 
	 * <code>(txtWorkflowGroup:Invoice)($ProcessID:1...)</code>
	 * 
	 * @param workitem - workItem to be tested
	 * @param filter   - combined regex to test different fields
	 * @return - true if filter matches filter expression.
	 */
	public static boolean matches(ItemCollection workitem, String filter) {

		if (filter == null || "".equals(filter.trim()))
			return true;

		// split columns
		StringTokenizer regexTokens = new StringTokenizer(filter, ")");
		while (regexTokens.hasMoreElements()) {
			String regEx = regexTokens.nextToken();
			// remove columns
			regEx=regEx.replace("(", "");
			regEx=regEx.replace(")", "");
			regEx=regEx.replace(",", "");
			// test if ':' found
			if (regEx.indexOf(':') > -1) {
				regEx = regEx.trim();
				// test if regEx contains "
				regEx=regEx.replace("\"", "");
				String itemName = regEx.substring(0, regEx.indexOf(':'));
				regEx = regEx.substring(regEx.indexOf(':') + 1);
				@SuppressWarnings("unchecked")
				List<Object> itemValues = workitem.getItemValue(itemName);
				for (Object aValue : itemValues) {
					if (!aValue.toString().matches(regEx)) {
						logger.fine("Value '" + aValue + "' did not match : " + regEx);
						return false;
					}
				}

			}
		}
		// workitem matches criteria
		return true;
	}
}
