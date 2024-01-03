package org.imixs.workflow.office.gdpr;

import java.util.List;
import java.util.logging.Logger;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.SignalAdapter;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.exceptions.AdapterException;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.exceptions.ProcessingErrorException;
import org.imixs.workflow.exceptions.QueryException;
import org.imixs.workflow.util.XMLParser;

import jakarta.inject.Inject;
import jakarta.mail.internet.AddressException;

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
 *   <gdpr>
		<delete-items>$file, txtname</delete-items>
		<anonymise-items>firstname,lastname</anonymise-items>
		<placeholder>no data</placeholder>
		<anonymise-references>true@false</anonymise-references>
	</gdpr>
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
public class GDPRAnonymiseAdapter implements SignalAdapter {
	private static Logger logger = Logger.getLogger(GDPRAnonymiseAdapter.class.getName());

	public static String PROCESSING_ERROR = "PROCESSING_ERROR";
	public static final String CONFIG_ERROR = "CONFIG_ERROR";

	@Inject
	GDPRAnonymiseService gdprAnonymiseService;

	@Inject
	DocumentService documentService;

	/**
	 * The method runs the plugin
	 * 
	 * @return
	 * @throws PluginException
	 * @throws AddressException
	 */
	@Override
	public ItemCollection execute(ItemCollection workitem, ItemCollection event)
			throws AdapterException, PluginException {

		List<ItemCollection> gdprConfigList = null;

		String workflowResult = event.getItemValueString("txtActivityResult");
		gdprConfigList = XMLParser.parseTagList(workflowResult, "gdpr");

		if (gdprConfigList == null || gdprConfigList.size() == 0) {
			throw new ProcessingErrorException(GDPRAnonymiseAdapter.class.getSimpleName(), CONFIG_ERROR,
					"missing gdpr configuraiton in BPMN event!");
		}

		// iterate over all gdpr configurations
		for (ItemCollection gdprConfig : gdprConfigList) {
			String deleteItems = gdprConfig.getItemValueString("delete-items");
			String anonymiseItems = gdprConfig.getItemValueString("anonymise-items");
			String placeholder = gdprConfig.getItemValueString("placeholder");
			boolean anonymiseReferences = gdprConfig.getItemValueBoolean("anonymise-workitemref");

			gdprAnonymiseService.deleteItems(workitem, deleteItems);
			gdprAnonymiseService.anonimiseItems(workitem, anonymiseItems, placeholder);

			// anonymise references?
			if (anonymiseReferences) {
				String searchTerm = "($workitemref:\"" + workitem.getUniqueID() + "\" )";
				try {
					List<ItemCollection> refList = documentService.find(searchTerm, 99999, 0);
					for (ItemCollection refWorkitem : refList) {
						gdprAnonymiseService.deleteItems(refWorkitem, deleteItems);
						gdprAnonymiseService.anonimiseItems(refWorkitem, anonymiseItems, placeholder);
						gdprAnonymiseService.save(refWorkitem);
					}
				} catch (QueryException e) {
					throw new PluginException(e.getErrorContext(), e.getErrorCode(), e.getMessage(), e);
				}
			}
		}

		return workitem;
	}

}