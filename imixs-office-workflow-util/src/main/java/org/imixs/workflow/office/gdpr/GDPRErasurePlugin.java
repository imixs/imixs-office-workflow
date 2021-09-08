package org.imixs.workflow.office.gdpr;

import java.util.logging.Logger;

import javax.mail.internet.AddressException;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.plugins.AbstractPlugin;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.util.XMLParser;

/**
 * The plugin alows the deletion or anonymisation of items within a running
 * workflow process. This is conform to * Art. 17 GDPR Right to erasure (‘right
 * to be forgotten’)
 * 
 * 
 * To trigger the process the following item definition is used:
 * 
 * <pre>
 * {@code
 * 
 *   <item name="gdpr-erasure">
          <delete>$file</items>
          <anonymize>firstname,lastname</items>
          <placeholder>no data</placeholder>
     </item>
 * 
 * }
 * </pre>
 * 
 * 
 * <p>
 * The item list can be customized for each event separately
 * 
 * @author rsoika
 * @version 2.0
 * 
 */
public class GDPRErasurePlugin extends AbstractPlugin {
	private static Logger logger = Logger.getLogger(GDPRErasurePlugin.class.getName());

	public static String PROCESSING_ERROR = "PROCESSING_ERROR";

	/**
	 * The method runs the plugin
	 * 
	 * @return
	 * @throws PluginException
	 * @throws AddressException
	 */
	@Override
	public ItemCollection run(ItemCollection documentContext, ItemCollection documentActivity) throws PluginException {

		// do we have a gdpr-erasure case?
		ItemCollection evalItemCollection = this.getWorkflowService().evalWorkflowResult(documentActivity,"item",
				documentContext);
		// find the gdpr item description
		if (evalItemCollection != null && evalItemCollection.hasItem("gdpr-erasure")) {
			String processDefinition = evalItemCollection.getItemValueString("gdpr-erasure");
			// evaluate the item content (XML format expected here!)
			ItemCollection processingData = XMLParser.parseItemStructure(processDefinition);

			String anonymizeItemList = processingData.getItemValueString("anonymize");
			String deleteItemList = processingData.getItemValueString("delete");
			String placeholder = processingData.getItemValueString("placeholder");

			// anonymize
			if (!anonymizeItemList.isEmpty()) {
				logger.finest("......anonymizing items for '" + documentContext.getUniqueID() + "'. Placeholder='"
						+ placeholder + "'");
				String[] gdprFieldList = anonymizeItemList.split(",");
				for (String field : gdprFieldList) {
					logger.finest("......anonymize item: " + field);
					documentContext.replaceItemValue(field, placeholder);
				}
			}
			// delete

			if (!deleteItemList.isEmpty()) {
				logger.finest("......removing items for '" + documentContext.getUniqueID() + "'");
				String[] dsgvoFieldList = deleteItemList.split(",");
				for (String field : dsgvoFieldList) {
					logger.finest("......delete item: " + field);
					documentContext.removeItem(field);
				}
			}
		}

		return documentContext;
	}

}