package org.imixs.workflow.office.dashboard;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.imixs.marty.team.TeamService;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.exceptions.QueryException;
import org.imixs.workflow.faces.data.WorkflowController;
import org.imixs.workflow.faces.util.LoginController;
import org.imixs.workflow.office.forms.AnalyticEvent;

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
    TeamService teamService;

    List<ItemCollection> invoices = null;
    int countAll = 0;
    int countFresh = 0; // Fresh tasks (0-3 days) - Blue
    int countNeedsAttention = 0; // Needs attention (3-7 days) - Orange
    int countUrgent = 0; // Urgent tasks (7+ days) - Red

    double totalAllCurrency1 = 0;
    double totalAllCurrency2 = 0;

    double totalOpenCurrency1 = 0;
    double totalOpenCurrency2 = 0;

    double totalDueCurrency1 = 0;
    double totalDueCurrency2 = 0;

    double totalDunningCurrency1 = 0;
    double totalDunningCurrency2 = 0;

    double averagePaymentDue = 0;
    double averagePaymentDays = 0;

    String chartData = "";

    public void onEvent(@Observes AnalyticEvent event) {

        // Recompute only if last dbtr.number has changed or no values yet computed

        // use cache?
        if (event.getWorkitem().hasItem(event.getKey())) {
            // logger.info(" use cache for " + event.getKey());
            // no op
            return;
        }

        logger.info("eventkey=" + event.getKey());
        calculateStats(event);

        if ("dashboard.workitems.count.all".equals(event.getKey())) {
            event.setValue("" + countAll);
            // event.setLabel("offene Aufgaben");
            event.setDescription("Meine offenen Aufgaben");
            event.setLink("/pages/notes_board.xhtml?viewType=worklist.owner");
        }
        if ("dashboard.workitems.count.fresh".equals(event.getKey())) {
            event.setValue("" + countFresh);
            // event.setLabel("neue Aufgaben");
            event.setDescription("Meine offenen Aufgaben");
            event.setLink("/pages/notes_board.xhtml?viewType=worklist.owner");
        }

        if ("dashboard.workitems.count.attention".equals(event.getKey())) {
            event.setValue("" + countNeedsAttention);
            // event.setLabel("Zu Beachten");
            event.setDescription("Aufgaben seit mehr als 3 Tagen offen");
            event.setLink("/pages/notes_board.xhtml?viewType=worklist.owner");
        }

        if ("dashboard.workitems.count.urgent".equals(event.getKey())) {
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

        totalAllCurrency1 = 0;
        totalAllCurrency2 = 0;
        totalOpenCurrency1 = 0;
        totalOpenCurrency2 = 0;
        totalDueCurrency1 = 0;
        totalDueCurrency2 = 0;
        totalDunningCurrency1 = 0;
        totalDunningCurrency2 = 0;

        // chartData = null;
        chartData = "{}";

        averagePaymentDue = 0;
        averagePaymentDays = 0;

        event.getWorkitem().removeItem("analytic.invoices.count.all");
        event.getWorkitem().removeItem("analytic.invoices.count.open");
        event.getWorkitem().removeItem("analytic.invoices.count.due");
        event.getWorkitem().removeItem("analytic.invoices.count.dunning");

        event.getWorkitem().removeItem("analytic.payment.avg.due");
        event.getWorkitem().removeItem("analytic.payment.avg.days");
        event.getWorkitem().removeItem("analytic.invoices.trend");

    }

    private String formatCurrency(Double value) {

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat formatter = new DecimalFormat("#,##0.00", symbols);

        return formatter.format(value);
    }

    /**
     * Hilfsmethode die auch alte Rechnungen ohne D/K findet
     * 
     * @return
     */
    private String getDbtNrQuery() {
        String dbtNr = workflowController.getWorkitem().getItemValueString("dbtr.number");
        String shortDbtNr = dbtNr.substring(1);
        String query = " (dbtr.number:" + dbtNr + " OR dbtr.number:" + shortDbtNr + ") ";
        return query;
    }

    private String getCdtrNrQuery() {
        String cdtrNr = workflowController.getWorkitem().getItemValueString("cdtr.number");
        String shortCdtrNr = cdtrNr.substring(1);
        String query = " (cdtr.number:" + cdtrNr + " OR cdtr.number:" + shortCdtrNr + ") ";
        return query;
    }

    /**
     * Finds the payment.date for a invoice.
     *
     * Wir suchen alle zugeordneten Zahlungseingägne und nehmen den letzten.
     *
     * WICHTIG: Es muss ggf. der index neu aufgebaut werden, da payment.date nun ein
     * index feld ist
     *
     * @param invoice
     * @return
     * @throws ParseException
     */
    public Date findPaymentDateByWorkitem(ItemCollection invoice) throws ParseException {

        // Wir selektieren alle Zahlungseingänge interessieren uns aber nur für den
        // letzten
        String sQuery = " (type:\"workitem\" OR type:\"workitemarchive\") " + //
                " AND ($modelversion:zahlungseingang-*) AND ($workitemref:\"" + invoice.getUniqueID() + "\" )";

        List<ItemCollection> workitems = null;

        try {
            workitems = documentService.findStubs(sQuery, 99, 0, "payment.date", true);
            if (workitems.size() > 0) {
                return workitems.get(0).getItemValueDate("payment.date");
            }
        } catch (QueryException e) {

            e.printStackTrace();
        }

        // no date found!
        return null;

    }

    /**
     * Diese Methode baut die Datenstruktur für das Chart Diagram zusammen
     *
     *
     * <pre>
    	{
    	 labels: ["January", "February", "March", "April", "May", "June", "July"],
    	 datasets: [{
    		label: 'Dataset 1',
    		//backgroundColor: color(window.chartColors.red).alpha(0.5).rgbString(),
    		//borderColor: window.chartColors.red,
    		borderWidth: 1,
    		data: [
    			70, 70, 70, 70, 79, 50, 50
    		]
    	}, {
    		label: 'Dataset 2',
    		//backgroundColor: color(window.chartColors.blue).alpha(0.5).rgbString(),
    		//borderColor: window.chartColors.blue,
    		borderWidth: 1,
    		data: [
    			70, 70, 170, 7, 79, 50, 50
    		]
    	}]
       }
     * </pre>
     *
     * @return
     */

    /**
     * returns
     *
     * 202304 from a given date
     */
    public String getYearMonth(Date date) {

        // Create a Calendar instance and set the date
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        // Get the year from the Calendar object
        int year = calendar.get(Calendar.YEAR);
        // Get the month from the Calendar object
        int month = calendar.get(Calendar.MONTH);
        // Increment the month by 1 since Calendar months are zero-based
        month++;
        // Convert the month to a String with leading "0" if necessary
        return "" + year + (month < 10 ? "0" + month : "" + month);

    }

}
