package org.imixs.marty.web.office;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.imixs.marty.web.workitem.WorkitemListener;
import org.imixs.marty.web.workitem.WorkitemMB;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.util.ItemCollectionAdapter;

/**
 * This bean provides a comment function to history comments entered by a user
 * into the field txtComment. The Bean creates the property txtCommentLog with a
 * Map providing following informations per each comment entry:
 * 
 * <ul>
 * <li>txtComment = comment</li>
 * <li>datComment = date of creation</li>
 * <li>namEditor = user name</li>
 * </ul>
 * 
 * The property log provides a ArrayList with ItemCollection Adapters providing
 * the comment details.
 * 
 * @author rsoika
 */
public class CommentMB implements WorkitemListener {
	private WorkitemMB workitemMB = null;
	private ItemCollection workitem=null;

	/**
	 * This method register the bean as an workitemListener
	 */
	@PostConstruct
	public void init() {
		// register this Bean as a workitemListener to the current WorktieMB
		this.getWorkitemBean().addWorkitemListener(this);
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

	public void onWorkitemChanged(ItemCollection arg0) {
		workitem=arg0;
	}

	/**
	 * updates the comment list Therefor the method copies the txtComment into
	 * the txtCommentList and clears the txtComment field
	 */
	public void onWorkitemProcess(ItemCollection workitem) {
		// take comment and append it to txtCommentList
		try {
			String sComment = workitem.getItemValueString("txtComment");
			if (!"".equals(sComment)) {
				Vector vCommentList = workitem.getItemValue("txtCommentLog");
				Map log = new HashMap();

				// create new Comment data
				log.put("txtcomment", sComment);
				Date dt = Calendar.getInstance().getTime();
				log.put("datcomment", dt);
				FacesContext context = FacesContext.getCurrentInstance();
				ExternalContext externalContext = context.getExternalContext();
				String remoteUser = externalContext.getRemoteUser();
				log.put("nameditor", remoteUser);
				vCommentList.add(0, log);

				workitem.replaceItemValue("txtcommentLog", vCommentList);

				// clear comment
				workitem.replaceItemValue("txtComment", "");
			}
		} catch (Exception e) {
			// unable to copy comment
			e.printStackTrace();
		}
	}

	public ArrayList<ItemCollectionAdapter> getLog() {
		ArrayList<ItemCollectionAdapter> commentList = new ArrayList<ItemCollectionAdapter>();
		if (workitem!=null) {
		try {
			Vector<Map> vCommentList = workitem.getItemValue("txtCommentLog");
			for (Map aworkitem : vCommentList) {
				ItemCollection aComment=new ItemCollection(aworkitem);
				commentList.add(new ItemCollectionAdapter(aComment));
			}
		} catch (Exception e) {
			// unable to copy comment
			//e.printStackTrace();
			try {
				workitem.replaceItemValue("txtcommentLog", "");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		}

		return commentList;
	}

	public void onChildProcess(ItemCollection arg0) {
		// nothing
	}

	public void onChildCreated(ItemCollection arg0) {
		// nothing

	}

	public void onChildProcessCompleted(ItemCollection arg0) {
		// nothing
	}

	public void onWorkitemCreated(ItemCollection arg0) {
		// nothing
	}

	public void onWorkitemProcessCompleted(ItemCollection arg0) {

	}

	public void onChildDelete(ItemCollection e) {
		// nothing
	}

	public void onChildDeleteCompleted() {
		// nothing
	}

	public void onChildSoftDelete(ItemCollection e) {
		// nothing
	}

	public void onChildSoftDeleteCompleted(ItemCollection e) {
		// nothing
	}

	public void onWorkitemDelete(ItemCollection e) {
		// nothing
	}

	public void onWorkitemDeleteCompleted() {
		// nothing
	}

	public void onWorkitemSoftDelete(ItemCollection e) {

	}

	public void onWorkitemSoftDeleteCompleted(ItemCollection e) {
		// nothing
	}

}