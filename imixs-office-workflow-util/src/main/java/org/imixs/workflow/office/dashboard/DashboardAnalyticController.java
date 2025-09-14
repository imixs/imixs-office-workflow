package org.imixs.workflow.office.dashboard;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
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
    int countToday = 0; // Fresh tasks today
    int countThisWeek = 0; // Needs attention (since Monday) - Orange
    int countOneWeek = 0; // Needs attention (since 1 week) - Orange
    int countUrgent = 0; // Urgent tasks (7+ days) - Red
    boolean calculatedStats = false;

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

        if (!calculatedStats) {
            calculateStats(event);
        }

        if ("dashboard.worklist.count.all".equals(event.getKey())) {
            event.setValue("" + countAll);
            // event.setLabel("offene Aufgaben");
            event.setDescription("Meine offenen Aufgaben");
            event.setLink("/pages/notes_board.xhtml?viewType=worklist.owner");
        }
        if ("dashboard.worklist.count.today".equals(event.getKey())) {
            event.setValue("" + countToday);
            // event.setLabel("neue Aufgaben");
            event.setDescription("Meine offenen Aufgaben");
            event.setLink("/pages/notes_board.xhtml?viewType=worklist.owner");
        }

        if ("dashboard.worklist.count.thisweek".equals(event.getKey())) {
            event.setValue("" + countThisWeek);
            // event.setLabel("Zu Beachten");
            event.setDescription("Aufgaben seit mehr als 3 Tagen offen");
            event.setLink("/pages/notes_board.xhtml?viewType=worklist.owner");
        }

        if ("dashboard.worklist.count.oneweek".equals(event.getKey())) {
            event.setValue("" + countOneWeek);
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
        countToday = 0; // Fresh tasks (0-3 days) - Blue
        countThisWeek = 0; // Needs attention (3-7 days) - Orange
        countUrgent = 0; // Urgent tasks (7+ days) - Red
        logger.info("	├──calculate stats for " + loginController.getUserPrincipal() + "....");

        // count tasks

        String searchTerm = "";

        try {
            searchTerm = "(type:workitem) AND ($owner:\"" + loginController.getUserPrincipal() + "\")";
            countAll = documentService.count(searchTerm);

            // Calculate threshold dates
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);
            LocalDate oneWeekAgo = today.minusWeeks(1);
            LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

            String todayStr = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String yesterdayStr = yesterday.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            // String threeDaysAgoStr =
            // threeDaysAgo.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String oneWeekAgoStr = oneWeekAgo.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String startOfWeekStr = startOfWeek.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            logger.info(" ├──Query ranges:");

            // Fresh tasks (today)
            String from = todayStr + "000000";
            String to = todayStr + "235959";
            countToday = countTasks(from, to);
            logger.info("   Today: [" + from + " TO " + to + "]");

            // Tasks needing attention (This week)
            from = startOfWeekStr + "000000";
            to = yesterdayStr + "235959";
            countThisWeek = countTasks(from, to);
            logger.info("   ThisWeek: [" + from + " TO " + to + "]");

            // Tasks needing attention (one week)
            from = oneWeekAgoStr + "000000";
            to = yesterdayStr + "235959";
            countOneWeek = countTasks(from, to);
            logger.info("   OneWeek: [" + from + " TO " + to + "]");

            // Urgent tasks (older than 1 Week) - from start TO oneWeekAgo-1
            from = "19700101000000";
            to = oneWeekAgoStr + "235959";
            countUrgent = countTasks(from, to);
            logger.info("   Urgent: [" + from + " TO " + to + "]");

            logger.info(" ├──Results: Today=" + countToday
                    + ", This Week=" + countThisWeek
                    + ", One Week=" + countOneWeek
                    + ", Urgent="
                    + countUrgent + ", Total=" + countAll);

            calculatedStats = true;
        } catch (QueryException e) {
            logger.log(Level.SEVERE, "getWorkListByOwner - invalid param: {0}", e.getMessage());

        }

    }

    /**
     * Helper method to count tasks by date range
     * 
     * @param from
     * @param to
     * @return
     * @throws QueryException
     */
    private int countTasks(String from, String to) throws QueryException {
        String searchTerm = "(type:workitem) AND ($owner:\"" + loginController.getUserPrincipal()
                + "\") AND ($lasteventdate:[" + from + " TO " + to + "])";
        return documentService.count(searchTerm);
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
