package org.imixs.application.minutes;

import java.util.Collection;
import java.util.logging.Logger;

import javax.mail.internet.AddressException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.WorkflowContext;
import org.imixs.workflow.exceptions.AccessDeniedException;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.jee.ejb.EntityService;
import org.imixs.workflow.jee.ejb.WorkflowService;
import org.imixs.workflow.plugins.jee.VersionPlugin;

/**
 * This Plugin extends the Version Plugin mechanism and manages child workitems
 * in the master version and version of a minute.
 * 
 * When a new version was created, all Child Workitems will copied into this
 * version and automatically archived. Child workitems in the current master
 * version, which are not archived or deleted will be removed from the current
 * version.
 * 
 * 
 * After a new version was created the property datDate will be deleted in the
 * current version.
 * 
 * SequenceNumber
 * 
 * The Plugin also computes a numsequencenumber for childWorkitems. There for
 * the plugins computes the child count of the parent woritem.
 * 
 * @author rsoika
 * @version 2.0
 * 
 */
public class MinutePlugin extends VersionPlugin {
	private EntityService entityService = null;
	private static Logger logger = Logger.getLogger(MinutePlugin.class
			.getName());

	public void init(WorkflowContext actx) throws PluginException {
		super.init(actx);

		// check for an instance of WorkflowService
		if (actx instanceof WorkflowService) {
			// yes we are running in a WorkflowService EJB
			WorkflowService ws = (WorkflowService) actx;
			entityService = ws.getEntityService();
		}

	}

	/**
	 * If a version was created, all childWorkitems will be copied into the
	 * version. Childs from the current master version which are archived or
	 * deleted will be removed.
	 * 
	 * To copy a childworkitem the plugin removes the current $UniqueID and
	 * replaces the $UniqueIDRef with the $UniqueId of the new Version. Next
	 * this workitem is saved. So it becomes a new entity which is a child to
	 * the version.
	 * 
	 * @return
	 * @throws PluginException
	 * @throws AddressException
	 */
	@Override
	public int run(ItemCollection documentContext,
			ItemCollection documentActivity) throws PluginException {

		// Compute a sequencenumber for new child workitems
		computeSequenceNumber(documentContext);

		// Versioning....
		int iResult = super.run(documentContext, documentActivity);

		// check if a Version was created
		ItemCollection version = this.getVersion();
		if (version != null) {
			logger.info("[MinutePlugin] creating new version....");
			String versionUnqiueID = version.getItemValueString("$uniqueid");
			String uniqueID = documentContext.getItemValueString("$uniqueid");

			// remove datDate from current workitem.
			documentContext.removeItem("datDate");

			// new version was created - now copy all minute items....
			// get childs of original
			Collection<ItemCollection> childs = this.getWorkflowService()
					.getWorkListByRef(uniqueID);
			for (ItemCollection achild : childs) {
				String sType = achild.getItemValueString("type");
				String sSummary = achild
						.getItemValueString("txtWorkflowSummary");
				try {

					// change ref id!
					achild.replaceItemValue("$uniqueidRef", versionUnqiueID);

					// when child not archived or deleted, create a new instance
					// and change
					// the type to archived
					if ((!sType.endsWith("deleted"))
							&& (!sType.endsWith("archive"))) {
						// it is an active workitem!
						// so we create a copy and attache it to the new version
						// the origin will not be touched!
						achild.replaceItemValue("type", sType + "archive");
						// duplicate workitem be removing the uniqueId
						achild.replaceItemValue("$uniqueid", "");
						logger.fine("[MinutePlugin] clone and archive child '"
								+ sSummary + "'");
					}

					// save child with runAs manager access level
					// this.getEntityService().save(achild);
					entityService.save(achild);

				} catch (AccessDeniedException e) {

					throw new PluginException("could not save", "SAVE_ERROR",
							e.getMessage(), e);
				}

			}

		}

		return iResult;
	}

	/**
	 * This method compute a new numsequencenumber for childworkitems. The next
	 * number is computed based on the count childwokitems.
	 * 
	 */
	private void computeSequenceNumber(ItemCollection documentContext) {

		// skip if it is no child or numsequencenumber already defined
		if (!"childworkitem".equals(documentContext.getItemValueString("Type"))
				|| documentContext.hasItem("numsequencenumber")) {
			logger.fine("[MinutePlugin] skip computeSequenceNumber");
			return;
		}

		// get parent workitem
		String sUniqueIdRef = documentContext
				.getItemValueString("$UniqueIDRef");

		logger.fine("[MinutePlugin] compute computeSequenceNumber for Ref:"
				+ sUniqueIdRef + "....");
		ItemCollection parent = this.getWorkflowService().getWorkItem(
				sUniqueIdRef);

		if (parent == null) {
			logger.warning("[MinutePlugin] parent not found!");
			return;
		}

		logger.fine("[MinutePlugin] compute new sequence number for childworkitem");

		// compute number by searching last childs.
		String sQuery = null;
		sQuery = "SELECT wi FROM Entity as wi ";
		sQuery += " JOIN wi.textItems as r ";
		sQuery += " JOIN wi.integerItems as n ";
		sQuery += " WHERE wi.type IN ('workitem','childworkitem','workitemarchive','childworkitemarchive')  ";
		sQuery += " AND r.itemName = '$uniqueidref' and r.itemValue = '"
				+ sUniqueIdRef + "'";
		sQuery += " AND n.itemName = 'numsequencenumber' ";
		sQuery += " ORDER BY n.itemValue DESC";

		logger.fine("[MinutePlugin] JPQL=" + sQuery);

		Collection<ItemCollection> childList = this.getWorkflowService()
				.getEntityService().findAllEntities(sQuery, 0, 1);

		int iNumber = 1;
		if (childList != null && childList.size() > 0) {
			ItemCollection child = childList.iterator().next();
			iNumber = child.getItemValueInteger("numsequencenumber");
			logger.fine("[MinutePlugin] last sequence number=" + iNumber);
			iNumber++;
		}
		documentContext.replaceItemValue("numsequencenumber", iNumber);

		// compute a text based super sequencenumber from parent
		// numsequencenumber
		// String sNumber = "" + parent.getItemValueInteger("numsequencenumber")
		// + "-" + iNumber;
		// documentContext.replaceItemValue("txtsequencenumber", sNumber);
		//
		// logger.fine("[MinutePlugin] txtsequencenumber="+sNumber);

	}
}
