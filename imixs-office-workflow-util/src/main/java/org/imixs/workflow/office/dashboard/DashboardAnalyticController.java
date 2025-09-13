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

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.exceptions.QueryException;
import org.imixs.workflow.faces.data.WorkflowController;
import org.imixs.workflow.faces.util.LoginController;
import org.imixs.workflow.office.config.SetupController;
import org.imixs.workflow.office.forms.AnalyticEvent;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;

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
    DashboardController dashboardController;

    List<ItemCollection> invoices = null;
    int countAll = 0;
    int countFresh = 0; // Fresh tasks (0-3 days) - Blue
    int countNeedsAttention = 0; // Needs attention (3-7 days) - Orange
    int countUrgent = 0; // Urgent tasks (7+ days) - Red

    String chartData = "";

    String lastCommand = "init";

    protected Map<String, DashboardDataSet> dataSets = null;

    @PostConstruct
    public void init() {
        dataSets = new HashMap<>();
    }

    public String getLastCommand() {
        return lastCommand;
    }

    public void setLastCommand(String lastCommand) {
        this.lastCommand = lastCommand;
    }

    public void onEvent(@Observes AnalyticEvent event) {

        // Recompute only if last dbtr.number has changed or no values yet computed

        // use cache?
        if (event.getWorkitem().hasItem(event.getKey())) {
            logger.fine(" use cache for " + event.getKey());
            // no op
            return;
        }

        logger.info("onEvent - calculateStats.... key=" + event.getKey());
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

            logger.info(" ├──Query ranges:");
            logger.info("   Fresh: [" + threeDaysAgoStr + " TO " + todayStr + "]");
            logger.info("   Attention: [" + oneWeekAgoStr + " TO " + threeDaysAgoMinus1 + "]");
            logger.info("   Urgent: [19700101 TO " + oneWeekAgoMinus1 + "]");
            logger.info(" ├──Results: Fresh=" + countFresh + ", Attention=" + countNeedsAttention + ", Urgent="
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
            DashboardDataSet dataSet = new DashboardDataSet(query, setupController.getPortletSize());

            try {
                dataSet.setData(
                        documentService.findStubs(dataSet.getQuery(), dataSet.getPageSize(), dataSet.getPageIndex(),
                                "$modified", true));
            } catch (QueryException e) {
                logger.warning("Failed to compute dataset: " + e.getMessage());
                dataSet.setData(new ArrayList<>());
            }
            this.dataSets.put(key, dataSet);
            return dataSet;

        }
        if ("dashboard.worklist.creator".equals(key)) {

            String query = "(type:\"workitem\" AND $creator:\"" + loginController.getRemoteUser() + "\")";
            DashboardDataSet dataSet = new DashboardDataSet(query, setupController.getPortletSize());
            try {
                dataSet.setData(
                        documentService.findStubs(dataSet.getQuery(), dataSet.getPageSize(), dataSet.getPageIndex(),
                                "$modified", true));
            } catch (QueryException e) {
                logger.warning("Failed to compute dataset: " + e.getMessage());
                dataSet.setData(new ArrayList<>());
            }
            this.dataSets.put(key, dataSet);
            return dataSet;
        }
        // not define - return empty data set
        return new DashboardDataSet("", 0);

    }

    public void ping() {
        logger.info(" ich pinge...");
    }

    public void executeCommand(String command) {
        logger.info(" ich copmande..." + command);
        lastCommand = command + " - " + System.currentTimeMillis();

        logger.info(" ich copmande..." + lastCommand);
    }

    public void nextOwner() {
        logger.info("ich navigier nach vorn");
        DashboardDataSet dataSet = getDataSet("dashboard.worklist.owner");
        try {
            dataSet.setPageIndex(dataSet.getPageIndex() + 1);
            dataSet.setData(
                    documentService.findStubs(dataSet.getQuery(), dataSet.getPageSize(), dataSet.getPageIndex(),
                            "$modified", true));
        } catch (QueryException e) {
            logger.warning("Failed to compute dataset: " + e.getMessage());
            dataSet.setData(new ArrayList<>());
        }

    }

}
