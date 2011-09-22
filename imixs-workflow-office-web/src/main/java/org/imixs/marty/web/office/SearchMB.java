package org.imixs.marty.web.office;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.imixs.marty.web.project.ProjectMB;
import org.imixs.marty.web.workitem.WorkitemMB;
import org.imixs.marty.web.workitem.WorklistMB;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.jee.ejb.EntityService;
import org.imixs.workflow.plugins.jee.extended.LucenePlugin;

/**
 * The minuteMB is a helper backing bean which holds the status of single minute
 * entries in the minute form to manage the display state of editor, history and
 * details. Only one of the child entries of a minute can be toggled once. It is
 * not possible to two different child entry with a toggle on at the same time.
 * <p>
 * Note: If one of the toggles is switched, than all other toggles will be
 * switched off automatically. Property: headerMode (true/false)
 * <p>
 * The bean holds also the status of the header of a minute to indicate if the
 * header should be displayed in edit or in 'document' mode.
 * <p>
 * The overall goal of this toggle switches is to display a hole minute in a
 * document style without any input fields - so the minute should not look like
 * a form but more as a document.
 * 
 * @author rsoika
 * 
 */
public class SearchMB  {
	private WorkitemMB workitemMB = null;
	private WorklistMB worklistMB = null;
	private ProjectMB projectMB = null;
	private ItemCollection searchFilter;
	private List<ItemCollection> workitems = null;
	private int count = 30;
	private int row = 0;
	private boolean endOfList = false;
	private static Logger logger = Logger.getLogger("org.imixs.workflow");

	/* Model Service */
	@EJB
	EntityService entityService;
	
	// Workflow Manager
	@EJB
	org.imixs.workflow.jee.ejb.WorkflowService workflowService;


	/**
	 * This method tries to load the config entity to read default values
	 */
	@PostConstruct
	public void init() {
		// initialize searchfilter
		doResetSearchFilter(null);
	}

	/**
	 * helper method to get the current instance of the WorkitemMB
	 * 
	 * @return
	 */
	private WorkitemMB getWorkitemBean() {
		if (workitemMB == null)
			workitemMB = (WorkitemMB) FacesContext.getCurrentInstance()
					.getApplication().getELResolver().getValue(
							FacesContext.getCurrentInstance().getELContext(),
							null, "workitemMB");
		return workitemMB;
	}

	private WorklistMB getWorkListBean() {
		if (worklistMB == null)
			worklistMB = (WorklistMB) FacesContext.getCurrentInstance()
					.getApplication().getELResolver().getValue(
							FacesContext.getCurrentInstance().getELContext(),
							null, "worklistMB");
		return worklistMB;
	}

	public ItemCollection getSearchFilter() {
		return searchFilter;
	}

	

	/**
	 * Creates a search term depending on the provided search fields.
	 * IndexFields will be search by the keyword search. Keyword search in
	 * lucene is case sensetive and did not allow wildcards!
	 * 
	 * Because of the restriction in kewordsearch we search the field
	 * 'txtsupliername' as a normal search phrase and not using the keyword
	 * search <br>
	 * So: 'Dell' will be found with 'dell' or 'de*'
	 * 
	 * @param event
	 */
	public void doSearch(ActionEvent event) {
		try {

			String sSearchTerm = "";

		
			Date datum = this.searchFilter.getItemValueDate("datDatum");
			if (datum != null) {
				SimpleDateFormat dateformat = new SimpleDateFormat(
						"yyyyMMddHHmm");

				// convert calendar to string
				String sDateValue = dateformat.format(datum);
				if (!"".equals(sDateValue))
					sSearchTerm += " (datdate:\"" + sDateValue + "\") AND";
			}

		
			String searchphrase = this.searchFilter
					.getItemValueString("txtSearch");
			
			if (!"".equals(searchphrase) ) {
				sSearchTerm += " (*" + searchphrase.toLowerCase() + "*)";

			} else
			// cut last AND
			if (sSearchTerm.endsWith("AND"))
				sSearchTerm = sSearchTerm
						.substring(0, sSearchTerm.length() - 3);

			workitems= LucenePlugin.search(
					sSearchTerm, workflowService);


		} catch (Exception e) {
			logger.warning("  lucene error!");
			e.printStackTrace();
		}

		
	}

	public List<ItemCollection> getWorkitems() {
		if (workitems == null)
			workitems = new ArrayList<ItemCollection>();
		return workitems;
	}

	public int getRow() {
		return row;
	}

	public boolean isEndOfList() {
		return endOfList;
	}
	
	/**
	 * resets the current project list and projectMB
	 * 
	 * @return
	 */
	public void doReset(ActionEvent event) {
		workitems = null;
		row = 0;
		searchFilter=new ItemCollection();
	}

	/**
	 * refreshes the current workitem list. so the list will be loaded again.
	 * but start pos will not be changed!
	 */
	public void doRefresh(ActionEvent event) {
		workitems = null;
	}

	public void doResetSearchFilter(ActionEvent event) {
		searchFilter = new ItemCollection();
		doSearch(event);
	}

	
	public ProjectMB getProjectBean() {
		if (projectMB == null)
			projectMB = (ProjectMB) FacesContext.getCurrentInstance()
					.getApplication().getELResolver().getValue(
							FacesContext.getCurrentInstance().getELContext(),
							null, "projectMB");
		return projectMB;
	}

	
	
	/**
	 * rebuilds the full text search index for all workitems
	 * @param event
	 * @throws Exception
	 */
	public void doRebuildFullTextIndex(ActionEvent event) throws Exception {
		int JUNK_SIZE = 100;
		long totalcount = 0;
		int startpos = 0;
		int icount = 0;
		boolean hasMoreData = true;

		// find all workitems
		long ltime = System.currentTimeMillis();
		//String sQuery = "SELECT entity FROM Entity entity WHERE entity.type IN ('workitem') ";
		String sQuery = "SELECT entity FROM Entity entity ";

		logger.info(" UpdateFulltextIndex starting....");

		while (hasMoreData) {
			// read a junk....
			Collection<ItemCollection> col = entityService.findAllEntities(
					sQuery, startpos, JUNK_SIZE);

			if (col.size() < JUNK_SIZE)
				hasMoreData = false;
			startpos = startpos + col.size();
			totalcount = totalcount + col.size();
			logger.info(" UpdateFulltextIndex - read " + totalcount
					+ " workitems....");

			icount = icount + col.size();
			// Update index
			LucenePlugin.addWorklist(col);

		}
		logger.info(" UpdateFulltextIndex finished - " + icount
				+ " workitems updated in "
				+ (System.currentTimeMillis() - ltime) + " ms");

	}
	
	
	
}