package org.imixs.workflow.office.dashboard;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.imixs.marty.profile.UserController;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.DocumentEvent;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.exceptions.QueryException;
import org.imixs.workflow.faces.data.WorkflowController;
import org.imixs.workflow.faces.util.LoginController;
import org.imixs.workflow.office.config.SetupController;
import org.imixs.workflow.office.forms.AnalyticEvent;
import org.imixs.workflow.office.forms.CustomFormController;

import jakarta.enterprise.context.Conversation;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Der DashboardAnalyticController computes the core analytic data for the
 * current user
 *
 * @author rsoika
 *
 */
@Named
@ConversationScoped
public class DashboardAnalyticController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(DashboardAnalyticController.class.getName());

    @Inject
    protected DocumentService documentService;

    @Inject
    protected WorkflowController workflowController;

    @Inject
    protected LoginController loginController;

    @Inject
    WorkflowService workflowService;
    @Inject
    SetupController setupController;

    @Inject
    private Conversation conversation;

    List<ItemCollection> invoices = null;
    int countAll = 0;
    int countFresh = 0; // Fresh tasks (0-3 days) - Blue
    int countNeedsAttention = 0; // Needs attention (3-7 days) - Orange
    int countUrgent = 0; // Urgent tasks (7+ days) - Red

    @Inject
    CustomFormController customFormController;

    @Inject
    UserController userController;

    protected Map<String, DashboardDataSet> dataSets = null;
    private ItemCollection workitem = new ItemCollection();
    private String setupConfigUniqueID = null;

    /**
     * This method loads the dashboard form information
     */
    public void initLayout() {
        dataSets = new HashMap<>();

        try {
            String content = setupController.getWorkitem().getItemValueString("dashboard.form");
            setupConfigUniqueID = setupController.getWorkitem().getUniqueID();
            workitem.setItemValue(CustomFormController.ITEM_CUSTOM_FORM, content);
            customFormController.computeFieldDefinition(workitem);
        } catch (ModelException e) {
            logger.warning("Failed to compute custom form sections: " + e.getMessage());
        }

    }

    /**
     * This method starts a new conversation scope, reset the data sets and loads
     * the dashboard.
     * This method is usually called in the dashboard.xhtml page
     */
    public void onLoad() {
        dataSets = new HashMap<>();
        startConversation();
        initLayout();
    }

    public ItemCollection getWorkitem() {
        return workitem;
    }

    public void setWorkitem(ItemCollection dashboardData) {
        this.workitem = dashboardData;
    }

    /**
     * Reacts on ON_DOCUMENT_SAVE of the config item and reloads the layout
     * 
     * @see DocumentEvent
     * @param documentEvent
     */
    public void onDocumentEvent(@Observes DocumentEvent documentEvent) {

        if (setupConfigUniqueID != null
                && documentEvent.getEventType() == DocumentEvent.ON_DOCUMENT_SAVE
                && setupConfigUniqueID.equals(documentEvent.getDocument().getUniqueID())) {
            logger.info("reload dashboard layout...");
            initLayout();
        }

    }

    /**
     * Starts a new conversation
     */
    protected void startConversation() {
        if (conversation.isTransient()) {
            conversation.setTimeout(
                    ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest())
                            .getSession().getMaxInactiveInterval() * 1000);
            conversation.begin();
            logger.log(Level.FINEST, "......start new conversation, id={0}", conversation.getId());
        }
    }

    public void onEvent(@Observes AnalyticEvent event) {

        // Recompute only if last dbtr.number has changed or no values yet computed

        // use cache?
        if (event.getWorkitem().hasItem(event.getKey())) {
            logger.fine(" use cache for " + event.getKey());
            // no op
            return;
        }

        logger.fine("onEvent - calculateStats.... key=" + event.getKey());
        calculateStats(event);

        if ("dashboard.worklist.count.all".equals(event.getKey())) {
            event.setValue("" + countAll);
            // event.setLabel("offene Aufgaben");
            event.setDescription("Meine offenen Aufgaben");
            event.setLink("/pages/notes_board.xhtml?viewType=worklist.owner");
        }
        if ("dashboard.worklist.count.fresh".equals(event.getKey())) {
            event.setValue("" + countFresh);
            // event.setLabel("neue Aufgaben");
            event.setDescription("Meine offenen Aufgaben");
            event.setLink("/pages/notes_board.xhtml?viewType=worklist.owner");
        }

        if ("dashboard.worklist.count.attention".equals(event.getKey())) {
            event.setValue("" + countNeedsAttention);
            // event.setLabel("Zu Beachten");
            event.setDescription("Aufgaben seit mehr als 3 Tagen offen");
            event.setLink("/pages/notes_board.xhtml?viewType=worklist.owner");
        }

        if ("dashboard.worklist.count.urgent".equals(event.getKey())) {
            event.setValue("" + countUrgent);
            // event.setLabel("Dringende Aufgaben");
            event.setDescription("Aufgaben seit mehr als 1 Woche offen");
            event.setLink("/pages/notes_board.xhtml?viewType=worklist.owner");
        }

    }

    /**
     * Läd die statistik daten zu einem debitor aus den aktuellen Rechnungen
     */
    private void calculateStats(AnalyticEvent event) {
        countAll = 0;
        countFresh = 0; // Fresh tasks (0-3 days) - Blue
        countNeedsAttention = 0; // Needs attention (3-7 days) - Orange
        countUrgent = 0; // Urgent tasks (7+ days) - Red
        logger.info("	├──calculate stats for " + loginController.getUserPrincipal() + "....");

        // count tasks

        String searchTerm = "";

        try {
            searchTerm = "(type:workitem) AND ($owner:\"" + loginController.getUserPrincipal() + "\")";
            countAll = documentService.count(searchTerm);

            // Calculate threshold dates
            LocalDate today = LocalDate.now();
            LocalDate threeDaysAgo = today.minusDays(3);
            LocalDate oneWeekAgo = today.minusWeeks(1);

            String todayStr = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String threeDaysAgoStr = threeDaysAgo.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String oneWeekAgoStr = oneWeekAgo.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            // Fresh tasks (last 3 days) - from threeDaysAgo TO today
            searchTerm = "(type:workitem) AND ($owner:\"" + loginController.getUserPrincipal()
                    + "\") AND ($lasteventdate:["
                    + threeDaysAgoStr + " TO " + todayStr + "])";
            countFresh = documentService.count(searchTerm);

            // Tasks needing attention (3-7 days) - from oneWeekAgo TO threeDaysAgo-1
            String threeDaysAgoMinus1 = threeDaysAgo.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            searchTerm = "(type:workitem) AND ($owner:\"" + loginController.getUserPrincipal()
                    + "\") AND ($lasteventdate:["
                    + oneWeekAgoStr + " TO " + threeDaysAgoMinus1 + "])";
            countNeedsAttention = documentService.count(searchTerm);

            // Urgent tasks (older than 7 days) - from start TO oneWeekAgo-1
            String oneWeekAgoMinus1 = oneWeekAgo.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            searchTerm = "(type:workitem) AND ($owner:\"" + loginController.getUserPrincipal()
                    + "\") AND ($lasteventdate:[19700101 TO " + oneWeekAgoMinus1 + "])";
            countUrgent = documentService.count(searchTerm);

            logger.fine(" ├──Query ranges:");
            logger.fine("   Fresh: [" + threeDaysAgoStr + " TO " + todayStr + "]");
            logger.fine("   Attention: [" + oneWeekAgoStr + " TO " + threeDaysAgoMinus1 + "]");
            logger.fine("   Urgent: [19700101 TO " + oneWeekAgoMinus1 + "]");
            logger.fine(" ├──Results: Fresh=" + countFresh + ", Attention=" + countNeedsAttention + ", Urgent="
                    + countUrgent + ", Total=" + countAll);

        } catch (QueryException e) {
            logger.log(Level.SEVERE, "getWorkListByOwner - invalid param: {0}", e.getMessage());

        }

    }

    /**
     * Setzt alle Statistikwerte zurück
     */
    private void resetStats(AnalyticEvent event) {
        logger.info("	├──reset stats....");

    }

    public DashboardDataSet getDataSet(String key) {
        DashboardDataSet result = null;
        result = dataSets.get(key);
        if (result == null) {
            result = calculateDataView(key);
        }
        return result;
    }

    private DashboardDataSet calculateDataView(String key) {
        // Data Views
        if ("dashboard.worklist.owner".equals(key)) {

            String query = "(type:\"workitem\" AND $owner:\"" + loginController.getRemoteUser() + "\")";
            DashboardDataSet dataSet = new DashboardDataSet(key, query, setupController.getPortletSize());
            loadData(dataSet);
            // try {

            // dataSet.setData(
            // documentService.findStubs(dataSet.getQuery(), dataSet.getPageSize(),
            // dataSet.getPageIndex(),
            // "$modified", true));
            // } catch (QueryException e) {
            // logger.warning("Failed to compute dataset: " + e.getMessage());
            // dataSet.setData(new ArrayList<>());
            // }
            this.dataSets.put(key, dataSet);
            return dataSet;

        }
        if ("dashboard.worklist.creator".equals(key)) {
            String query = "(type:\"workitem\" AND $creator:\"" + loginController.getRemoteUser() + "\")";
            DashboardDataSet dataSet = new DashboardDataSet(key, query, setupController.getPortletSize());
            // try {
            loadData(dataSet);
            // dataSet.setData(
            // documentService.findStubs(dataSet.getQuery(), dataSet.getPageSize(),
            // dataSet.getPageIndex(),
            // "$modified", true));
            // } catch (QueryException e) {
            // logger.warning("Failed to compute dataset: " + e.getMessage());
            // dataSet.setData(new ArrayList<>());
            // }
            this.dataSets.put(key, dataSet);
            return dataSet;
        }
        // not define - return empty data set
        return new DashboardDataSet("none", "", 0);

    }

    /**
     * Navigate in a data set to the next page by increasing the page index
     * 
     * @param key
     */
    public void next(DashboardDataSet dataSet) {
        if (dataSet != null) {
            // try {
            dataSet.setPageIndex(dataSet.getPageIndex() + 1);
            // dataSet.setData(
            // documentService.findStubs(dataSet.getQuery(), dataSet.getPageSize(),
            // dataSet.getPageIndex(),
            // "$modified", true));
            // } catch (QueryException e) {
            // logger.warning("Failed to compute dataset: " + e.getMessage());
            // dataSet.setData(new ArrayList<>());
            // }
            loadData(dataSet);
        }

    }

    /**
     * Navigate in a data set to the previous page by increasing the page index
     * 
     * @param key
     */
    public void prev(DashboardDataSet dataSet) {
        if (dataSet != null) {
            // try {
            dataSet.setPageIndex(dataSet.getPageIndex() - 1);
            // dataSet.setData(
            // documentService.findStubs(dataSet.getQuery(), dataSet.getPageSize(),
            // dataSet.getPageIndex(),
            // "$modified", true));
            loadData(dataSet);
            // } catch (QueryException e) {
            // logger.warning("Failed to compute dataset: " + e.getMessage());
            // dataSet.setData(new ArrayList<>());
            // }
        }

    }

    private void loadData(DashboardDataSet dataSet) {
        if (dataSet != null) {
            try {
                dataSet.setData(
                        documentService.findStubs(dataSet.getQuery(), dataSet.getPageSize(), dataSet.getPageIndex(),
                                "$modified", true));

                // The end of a list is reached when the size is below or equal the
                // pageSize. See issue #287
                dataSet.setTotalCount(documentService.count(dataSet.getQuery()));
                dataSet.setTotalPages((int) Math.ceil((double) dataSet.getTotalCount() / dataSet.getPageSize()));
                if (dataSet.getData().size() < dataSet.getPageSize()) {
                    dataSet.setEndOfList(true);
                } else {
                    // look ahead if we have more entries...
                    int iAhead = (dataSet.getPageSize() * dataSet.getPageIndex() + 1) + 1;
                    if (dataSet.getTotalCount() < iAhead) {
                        // there is no more data
                        dataSet.setEndOfList(true);
                    } else {
                        dataSet.setEndOfList(false);
                    }
                }
            } catch (QueryException e) {
                logger.warning("Failed to compute dataset: " + e.getMessage());
                dataSet.setData(new ArrayList<>());
            }
        }

    }

}
