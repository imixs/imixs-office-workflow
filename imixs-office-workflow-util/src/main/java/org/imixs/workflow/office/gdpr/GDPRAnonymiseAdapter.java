package org.imixs.workflow.office.gdpr;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.SignalAdapter;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.engine.ProcessingEvent;
import org.imixs.workflow.engine.index.SearchService;
import org.imixs.workflow.engine.plugins.SplitAndJoinPlugin;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.exceptions.AdapterException;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.exceptions.ProcessingErrorException;
import org.imixs.workflow.exceptions.QueryException;
import org.imixs.workflow.util.XMLParser;

import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.mail.internet.AddressException;

/**
 * The plugin allows the deletion or anonymize of items within a running
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
		<delete>$file, txtname</delete>
		<anonymise>firstname,lastname</anonymise>
		<placeholder>no data</placeholder>
		<references>IN|OUT|ALL|NONE</references>
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
	 * The method stores the current gdp result in the item 'gdpr.definiton'. This
	 * information is used in the workflow event 'WORKITEM_AFTER_PROCESS' to remove
	 * the items after the normal processing live cycle.
	 * 
	 * Otherwise removing or anonymizing items controlled by a plugin would be
	 * overwritten by the plugins in the normal processing live-cycle.
	 * 
	 * @return
	 * @throws PluginException
	 * @throws AddressException
	 */
	@Override
	public ItemCollection execute(ItemCollection workitem, ItemCollection event)
			throws AdapterException, PluginException {

		// we store only the result configuration for later processing
		String workflowResult = event.getItemValueString("txtActivityResult");
		workitem.setItemValue("gdpr.definition", workflowResult);
		return workitem;
	}

	/**
	 * WorkflowEvent listener to anonymize / delete the items defined in the
	 * workflow result.
	 * 
	 * @param processingEvent
	 * @throws AccessDeniedException
	 * @throws ModelException
	 */
	@SuppressWarnings("unchecked")
	public void onWorkflowEvent(@Observes ProcessingEvent processingEvent) {
		if (processingEvent == null || processingEvent.getDocument() == null) {
			return;
		}

		ItemCollection workitem = processingEvent.getDocument();

		int eventType = processingEvent.getEventType();
		if (ProcessingEvent.AFTER_PROCESS == eventType && workitem.hasItem("gdpr.definition")) {
			try {
				logger.info(" after process - now we can remove");

				List<ItemCollection> gdprConfigList = null;

				String workflowResult = workitem.getItemValueString("gdpr.definition");
				// remove the gdpr.definition immediately to avoid later processing
				workitem.removeItem("gdpr.definition");
				gdprConfigList = XMLParser.parseTagList(workflowResult, "gdpr");

				if (gdprConfigList == null || gdprConfigList.size() == 0) {
					throw new ProcessingErrorException(GDPRAnonymiseAdapter.class.getSimpleName(), CONFIG_ERROR,
							"missing gdpr configuration in BPMN event!");
				}

				// iterate over all gdpr configurations references
				for (ItemCollection gdprConfig : gdprConfigList) {
					List<String> outgoinReferences = new ArrayList<String>();

					String deleteItems = gdprConfig.getItemValueString("delete");
					String anonymiseItems = gdprConfig.getItemValueString("anonymise");
					String placeholder = gdprConfig.getItemValueString("placeholder");
					String anonymiseReferences = gdprConfig.getItemValueString("references");

					gdprAnonymiseService.deleteItems(workitem, deleteItems);
					gdprAnonymiseService.anonimiseItems(workitem, anonymiseItems, placeholder);

					// anonymise references?
					if ("OUT".equals(anonymiseReferences) || "ALL".equals(anonymiseReferences)) {
						// anonymize all sub processes....
						logger.info("...anonymise outgoing references...");
						outgoinReferences = workitem.getItemValue(SplitAndJoinPlugin.LINK_PROPERTY);
						for (String refid : outgoinReferences) {
							ItemCollection refWorkitem = documentService.load(refid);
							if (refWorkitem != null) {
								gdprAnonymiseService.deleteItems(refWorkitem, deleteItems);
								gdprAnonymiseService.anonimiseItems(refWorkitem, anonymiseItems, placeholder);
								gdprAnonymiseService.save(refWorkitem);
							}
						}
					}
					if ("IN".equals(anonymiseReferences) || "ALL".equals(anonymiseReferences)) {
						logger.info("...anonymise ingoing references...");
						String searchTerm = "($workitemref:\"" + workitem.getUniqueID() + "\" )";
						try {
							List<ItemCollection> refList = documentService.find(searchTerm,
									SearchService.DEFAULT_MAX_SEARCH_RESULT, 0);
							for (ItemCollection refWorkitem : refList) {
								// verify if not yet anonymized....
								if (!outgoinReferences.contains(refWorkitem.getUniqueID())) {
									gdprAnonymiseService.deleteItems(refWorkitem, deleteItems);
									gdprAnonymiseService.anonimiseItems(refWorkitem, anonymiseItems, placeholder);
									gdprAnonymiseService.save(refWorkitem);
								}
							}
						} catch (QueryException e) {
							throw new PluginException(e.getErrorContext(), e.getErrorCode(), e.getMessage(), e);
						}
					}
				}

			} catch (PluginException e) {
				// fire a runtime Exception
				throw new ProcessingErrorException(e.getErrorContext(), e.getErrorCode(), e.getMessage(), e);
			}

		}

	}

}