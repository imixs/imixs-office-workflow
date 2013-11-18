package org.imixs.minutes;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.jee.ejb.WorkflowService;
import org.imixs.workflow.jee.faces.util.LoginController;
import org.imixs.workflow.jee.faces.workitem.ViewController;
import org.imixs.workflow.jee.faces.workitem.WorklistController;

/**
 * This Controller extends the Imixs WorklistController and provides two
 * different views to display Minutes and Issues of a minute.
 * 
 * @author rsoika
 * 
 */
public class MinuteWorklistController extends WorklistController {


	final String QUERY_MINUTES_BY_OWNER = "minutes.minutes.owner";
	final String QUERY_TASKS_BY_OWNER = "minutes.tasks.owner";
	private static final long serialVersionUID = 1L;

	
	@Inject
	private LoginController loginController;


	public MinuteWorklistController() {
		super();
		setViewAdapter(new MinuteViewAdapter());
	}

	

	/**
	 * Custom implementation of a ViewAdapter to return workflow specific result
	 * lists.
	 * 
	 * @author rsoika
	 * 
	 */
	class MinuteViewAdapter extends ViewAdapter {

		public List<ItemCollection> getViewEntries(
				final ViewController controller) {
			if (QUERY_MINUTES_BY_OWNER.equals(getView())) {
				String name;

				name = loginController.getUserPrincipal();

				String sQuery = null;
				sQuery = "SELECT wi FROM Entity as wi ";
				sQuery += " JOIN wi.textItems as o ";
				sQuery += " WHERE wi.type='workitem'  ";
				sQuery += " AND o.itemName = 'namowner' and o.itemValue = '"
						+ name + "'" + createSortOrderClause(getSortOrder());

				return controller.getEntityService().findAllEntities(sQuery,
						controller.getRow(), controller.getMaxResult());

			}

			if (QUERY_TASKS_BY_OWNER.equals(getView())) {
				String name;

				name = loginController.getUserPrincipal();

				String sQuery = null;
				sQuery = "SELECT wi FROM Entity as wi ";
				sQuery += " JOIN wi.textItems as o ";
				sQuery += " WHERE wi.type='childworkitem'  ";
				sQuery += " AND o.itemName = 'namowner' and o.itemValue = '"
						+ name + "'";
				sQuery += createSortOrderClause(getSortOrder());

				return controller.getEntityService().findAllEntities(sQuery,
						controller.getRow(), controller.getMaxResult());

			}

			// default empty list

			return new ArrayList<ItemCollection>();

		}

		/**
		 * generates a sort order clause depending on a sororder id
		 * 
		 * @param asortorder
		 * @return
		 */
		private String createSortOrderClause(int asortorder) {
			switch (asortorder) {

			case WorkflowService.SORT_ORDER_CREATED_ASC: {
				return " ORDER BY wi.created asc";
			}
			case WorkflowService.SORT_ORDER_MODIFIED_ASC: {
				return " ORDER BY wi.modified asc";
			}
			case WorkflowService.SORT_ORDER_MODIFIED_DESC: {
				return " ORDER BY wi.modified desc";
			}
			default:
				return " ORDER BY wi.created desc";
			}

		}
	}

}
