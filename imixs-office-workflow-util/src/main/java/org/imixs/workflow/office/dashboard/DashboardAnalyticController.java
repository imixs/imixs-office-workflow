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
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.exceptions.QueryException;
import org.imixs.workflow.faces.data.WorkflowController;
import org.imixs.workflow.faces.util.LoginController;
import org.imixs.workflow.office.config.SetupController;
import org.imixs.workflow.office.forms.AnalyticController;
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
 * The DashboardAnalyticController computes the core analytic data for the
 * current user.
 * 
 * 
 * @author rsoika
 *
 */
@Named
@ConversationScoped
public class DashboardAnalyticController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(DashboardAnalyticController.class.getName());

    int countAllByOwner = 0;
    int countAllByParticipant = 0;
    int countAllByCreator = 0;
    int countTodayByOwner = 0; // Fresh tasks today
    int countThisWeekByOwner = 0; // Needs attention (since Monday) - Orange
    int countOneWeek = 0; // Needs attention (since 1 week) - Orange
    int countUrgentByOwner = 0; // Urgent tasks (7+ days) - Red
    boolean calculatedStats = false;

    protected Map<String, DashboardDataSet> dataSets = null;
    private ItemCollection workitem = new ItemCollection();

    private static final int PORTLET_SIZE = 5;
    public static final String LINK_PROPERTY = "$workitemref";

    @Inject
    CustomFormController customFormController;

    @Inject
    UserController userController;

    @Inject
    protected DocumentService documentService;

    @Inject
    protected WorkflowController workflowController;

    @Inject
    SetupController setupController;

    @Inject
    protected LoginController loginController;

    @Inject
    WorkflowService workflowService;

    @Inject
    private Conversation conversation;

    @Inject
    SetupController configController;

    @Inject
    protected AnalyticController analyticController;

    public static String DASHBOARD_DEFAULT_DEFINITION = "<imixs-form>\n" + //
            "  <imixs-form-section columns=\"4\" label=\"\">\n" + //
            "     <item name=\"dashboard.worklist.owner.count.today\" type=\"custom\" path=\"cards/plain\"  label=\"\"\n"
            + //
            "           options='{\"class\":\"flat success\", \"icon\":\"fa-inbox\", \"label\":\"Neue Aufgaben\", \"description\":\"Neue Aufgaben seit Heute\"}'   />\n"
            + //
            "     <item name=\"dashboard.worklist.owner.count.oneweek\" type=\"custom\" path=\"cards/plain\"  label=\"\"\n"
            + //
            "           options='{\"class\":\"flat warning\", \"icon\":\"fa-exclamation-triangle\", \"label\":\"Zu Beachten\", \"description\":\"Aufgaben seit einer Woche offen\"}'    />\n"
            + //
            "     <item name=\"dashboard.worklist.owner.count.urgent\" type=\"custom\" path=\"cards/plain\"  label=\"\"\n"
            + //
            "           options='{\"class\":\"flat error\", \"icon\":\"fa-fire\", \"label\":\"Dringend\", \"description\":\"Aufgaben seit mehr als 1 Woche offen\"}'    />\n"
            + //
            "    <item name=\"dashboard.worklist.participant.count.all\" type=\"custom\" path=\"cards/plain\" label=\"\"\n"
            + //
            "           options='{\"class\":\"flat\", \"icon\":\"fa-tasks\", \"label\":\"Meine Vorgänge\", \"description\":\"Offene Vorgänge mit Ihrer Beteiligung.\"}' />\n"
            + //
            "   </imixs-form-section>\n" + //
            "  <imixs-form-section columns=\"2\">\n" + //
            "    <item name=\"dashboard.worklist.owner\" type=\"custom\" path=\"cards/worklist\"\n" + //
            "         options='{ \"label\":\"Meine Aufgaben\", \"description\":\"Aufgaben, die Ihrer Bearbeitung bedürfen.\", \"pagesize\":\"10\"}'/>\n"
            + //
            "    <item name=\"dashboard.worklist.creator\" type=\"custom\" path=\"cards/worklist\"\n" + //
            "         options='{ \"label\":\"Meine Vorgänge\", \"description\":\"Aufgaben, die von Ihnen erstellt wurden.\", \"pagesize\":\"10\"}'/>\n"
            + //
            "  </imixs-form-section>\n" + //
            "</imixs-form> ";

    /**
     * This method loads the dashboard layout information and stores the layout in
     * the item 'txtWorkflowEditorCustomForm'.
     * <p>
     * The dashboard layout can be stored in the global settings as also in the user
     * UserProfile. This mechanism allows to configure custom dashboards for each
     * user.
     */
    public void initLayout() {
        dataSets = new HashMap<>();
        // first test if the user profile has a dashboard.form defined
        String content = userController.getWorkitem().getItemValueString("dashboard.form");
        if (content.isBlank() || !content.trim().startsWith("<imixs-form")) {
            // no user layout defined, so we load the default layout
            content = loadDefaultLayoutConfiguration();
        }

        try {
            // set custom form layout
            workitem.setItemValue(CustomFormController.ITEM_CUSTOM_FORM, content);
            customFormController.computeFieldDefinition(workitem);
        } catch (ModelException e) {
            logger.warning("Failed to compute custom form sections: " + e.getMessage());
        }

    }

    /**
     * This method loads the default layout from the BASIC configuration
     */
    private String loadDefaultLayoutConfiguration() {
        ItemCollection configItemCollection = configController.getWorkitem();
        // Test if a 'dashboard.from' is defined. If not, we set the default from here.
        if (configItemCollection.getItemValueString("dashboard.form").isBlank()) {
            // set default form
            return DASHBOARD_DEFAULT_DEFINITION;
        }
        return configItemCollection.getItemValueString("dashboard.form");
    }

    /**
     * This method starts a new conversation scope, reset the data sets and loads
     * the dashboard. This method is usually called in the dashboard.xhtml page
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

        // Run only for dashboard keys
        if (!event.getKey().startsWith("dashboard.")) {
            return;
        }

        // use cache?
        if (event.getWorkitem().hasItem(event.getKey())) {
            logger.fine(" use cache for " + event.getKey());
            // no op
            return;
        }
        if (!loginController.isAuthenticated()) {
            return;
        }

        logger.fine("onEvent - calculateStats.... key=" + event.getKey());

        if (!calculatedStats) {
            calculateStats(event);
        }

        // read options
        String options = event.getOptions();
        if (options == null || options.isBlank()) {
            logger.warning("Missing options for analytic part '" + event.getKey() + "' ");
            options = "{}";
        }

        if ("dashboard.worklist.owner.count.all".equals(event.getKey())) {
            event.setData(new ItemCollection()
                    .setItemValue("value", countAllByOwner)
                    .setItemValue("label",
                            analyticController.getOption(event.getKey(), "label", options, "Neue Aufgaben"))
                    .setItemValue("description",
                            analyticController.getOption(event.getKey(), "description", options, ""))
                    .setItemValue("link", "/pages/notes_board.xhtml?viewType=worklist.owner"));

        }
        if ("dashboard.worklist.creator.count.all".equals(event.getKey())) {

            event.setData(new ItemCollection()
                    .setItemValue("value", countAllByOwner)
                    .setItemValue("label",
                            analyticController.getOption(event.getKey(), "label", options, "Meine Vorgänge"))
                    .setItemValue("description",
                            analyticController.getOption(event.getKey(), "description", options, "Meine Vorgänge"))
                    .setItemValue("link", "/pages/notes_board.xhtml?viewType=worklist.creator"));

        }

        if ("dashboard.worklist.participant.count.all".equals(event.getKey())) {
            event.setData(new ItemCollection()
                    .setItemValue("value", countAllByParticipant)
                    .setItemValue("label",
                            analyticController.getOption(event.getKey(), "label", options, "Meine Vorgänge"))
                    .setItemValue("description",
                            analyticController.getOption(event.getKey(), "description", options, "Meine Vorgänge"))
                    .setItemValue("link", "/pages/notes_board.xhtml?viewType=worklist.participant"));
        }

        if ("dashboard.worklist.owner.count.today".equals(event.getKey())) {

            event.setData(new ItemCollection()
                    .setItemValue("value", countTodayByOwner)
                    .setItemValue("label",
                            analyticController.getOption(event.getKey(), "label", options, "Meine Vorgänge"))
                    .setItemValue("description",
                            analyticController.getOption(event.getKey(), "description", options,
                                    "Meine offenen Aufgaben"))
                    .setItemValue("link", "/pages/notes_board.xhtml?viewType=worklist.owner"));

        }

        if ("dashboard.worklist.owner.count.thisweek".equals(event.getKey())) {

            event.setData(new ItemCollection()
                    .setItemValue("value", countThisWeekByOwner)
                    .setItemValue("label",
                            analyticController.getOption(event.getKey(), "label", options, "Zu Beachten"))
                    .setItemValue("description",
                            analyticController.getOption(event.getKey(), "description", options,
                                    "Aufgaben seit mehr als 3 Tagen offen"))
                    .setItemValue("link", "/pages/notes_board.xhtml?viewType=worklist.owner"));

        }

        if ("dashboard.worklist.owner.count.oneweek".equals(event.getKey())) {
            event.setData(new ItemCollection()
                    .setItemValue("value", countOneWeek)
                    .setItemValue("label",
                            analyticController.getOption(event.getKey(), "label", options, "Zu Beachten"))
                    .setItemValue("description",
                            analyticController.getOption(event.getKey(), "description", options,
                                    "Aufgaben seit mehr als 3 Tagen offen"))
                    .setItemValue("link", "/pages/notes_board.xhtml?viewType=worklist.owner"));

        }

        if ("dashboard.worklist.owner.count.urgent".equals(event.getKey())) {

            event.setData(new ItemCollection()
                    .setItemValue("value", countUrgentByOwner)
                    .setItemValue("label",
                            analyticController.getOption(event.getKey(), "label", options, "Dringende Aufgaben"))
                    .setItemValue("description",
                            analyticController.getOption(event.getKey(), "description", options,
                                    "Aufgaben seit mehr als 1 Woche offen"))
                    .setItemValue("link", "/pages/notes_board.xhtml?viewType=worklist.owner"));

        }

    }

    /**
     * loads statistic data
     */
    private void calculateStats(AnalyticEvent event) {
        countAllByOwner = 0;
        countAllByCreator = 0;
        countAllByParticipant = 0;
        countTodayByOwner = 0; // Fresh tasks (0-3 days) - Blue
        countThisWeekByOwner = 0; // Needs attention (3-7 days) - Orange
        countUrgentByOwner = 0; // Urgent tasks (7+ days) - Red
        logger.info("├──calculate stats for " + loginController.getUserPrincipal() + "....");

        // count tasks
        try {
            countAllByOwner = documentService.count("(type:workitem) AND ($owner:\"" +
                    loginController.getUserPrincipal() + "\")");
            countAllByCreator = documentService.count("(type:workitem) AND ($creator:\"" +
                    loginController.getUserPrincipal() + "\")");
            countAllByParticipant = documentService.count("(type:workitem) AND ($participants:\"" +
                    loginController.getUserPrincipal() + "\")");

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

            logger.info("├──Query ranges:");

            // Fresh tasks (today)
            String from = todayStr + "000000";
            String to = todayStr + "235959";
            countTodayByOwner = countTasks(from, to);
            logger.info("   Today: [" + from + " TO " + to + "]");

            // Tasks needing attention (This week)
            from = startOfWeekStr + "000000";
            to = yesterdayStr + "235959";
            countThisWeekByOwner = countTasks(from, to);
            logger.info("   ThisWeek: [" + from + " TO " + to + "]");

            // Tasks needing attention (one week)
            from = oneWeekAgoStr + "000000";
            to = yesterdayStr + "235959";
            countOneWeek = countTasks(from, to);
            logger.info("   OneWeek: [" + from + " TO " + to + "]");

            // Urgent tasks (older than 1 Week) - from start TO oneWeekAgo-1
            from = "19700101000000";
            to = oneWeekAgoStr + "235959";
            countUrgentByOwner = countTasks(from, to);
            logger.info("   Urgent: [" + from + " TO " + to + "]");

            logger.info("├── Results: Today=" + countTodayByOwner
                    + ", This Week=" + countThisWeekByOwner
                    + ", One Week=" + countOneWeek
                    + ", Urgent="
                    + countUrgentByOwner + ", Total=" + countAllByOwner);

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

    public DashboardDataSet getDataSet(String key, String options) {
        // Test if we have options with a pageSize
        int pageSize = PORTLET_SIZE;
        if (options != null && !options.isBlank()) {
            try {
                String _pageSize = analyticController.getOption(key, "pagesize", options, "" + PORTLET_SIZE);
                if (_pageSize != null) {
                    pageSize = Integer.parseInt(_pageSize);
                }
            } catch (NumberFormatException e) {
                pageSize = PORTLET_SIZE;
            }
        }
        DashboardDataSet result = null;
        result = dataSets.get(key);
        if (result == null) {
            // Initialize a new DashboardDataSet
            result = initDashboardDataSet(key, pageSize);
        }
        return result;
    }

    /**
     * Initializes a Dataset for a given key. The parameter pageSize can be set.
     * 
     * @param key
     * @param pageSize
     * @return
     */
    private DashboardDataSet initDashboardDataSet(String key, int pageSize) {
        logger.info("├── init worklist: " + key);
        // Data Views
        if ("dashboard.worklist.owner".equals(key)) {
            String query = "(type:\"workitem\" AND $owner:\"" + loginController.getRemoteUser() + "\")";
            DashboardDataSet dataSet = new DashboardDataSet(key, query, pageSize);
            loadData(dataSet);
            this.dataSets.put(key, dataSet);
            return dataSet;

        }
        if ("dashboard.worklist.creator".equals(key)) {
            String query = "(type:\"workitem\" AND $creator:\"" + loginController.getRemoteUser() + "\")";
            DashboardDataSet dataSet = new DashboardDataSet(key, query, pageSize);
            loadData(dataSet);
            this.dataSets.put(key, dataSet);
            return dataSet;
        }
        if ("dashboard.worklist.participant".equals(key)) {
            String query = "(type:\"workitem\" AND $participants:\"" + loginController.getRemoteUser() + "\")";
            DashboardDataSet dataSet = new DashboardDataSet(key, query, pageSize);
            loadData(dataSet);
            this.dataSets.put(key, dataSet);
            return dataSet;
        }

        if ("dashboard.worklist.favorite".equals(key)) {
            List<String> favorites = userController.getWorkitem().getItemValue(LINK_PROPERTY);
            if (favorites.size() == 0) {
                favorites.add("NONE"); // dummy entry
            }
            String query = "(type:\"workitem\" OR type:\"workitemarchive\") AND ";
            // create IN list
            query += " ( ";
            for (String aID : favorites) {
                query += "$uniqueid:\"" + aID + "\" OR ";
            }
            // cut last ,
            query = query.substring(0, query.length() - 3);
            query += ")";

            DashboardDataSet dataSet = new DashboardDataSet(key, query, pageSize);
            loadData(dataSet);
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
            dataSet.setPageIndex(dataSet.getPageIndex() + 1);
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
            dataSet.setPageIndex(dataSet.getPageIndex() - 1);
            loadData(dataSet);
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
