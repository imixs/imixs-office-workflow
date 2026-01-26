package org.imixs.workflow.office.dashboard;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.imixs.marty.team.TeamService;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.exceptions.QueryException;
import org.imixs.workflow.office.forms.AnalyticController;
import org.imixs.workflow.office.forms.AnalyticEvent;
import org.imixs.workflow.office.model.ModelController;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

/**
 * The ProcessAnalyticController provides generic char data for a process. A
 * process can be defined by the options parameter 'process=<PROCESSNAME>'
 *
 * @author rsoika
 *
 */
@Named
@RequestScoped
public class ProcessAnalyticController implements Serializable {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(ProcessAnalyticController.class.getName());
	public static final String CHILD_ITEM_PROPERTY = "_ChildItems";

	@Inject
	protected DocumentService documentService;

	@Inject
	protected AnalyticController analyticController;

	@Inject
	protected ModelController modelController;

	@Inject
	TeamService teamService;

	List<MonthStats> monthStatsList;

	/**
	 * Computes process analytic data. The event key must start with
	 * 'process.stats.chart.' followed by a unique identifier. e.g
	 * 'process.stats.chart.Sales'
	 * 
	 * @param event
	 */
	public void onEvent(@Observes AnalyticEvent event) {

		if (!event.getKey().startsWith("worklist.stats.")) {
			return;
		}
		// read options
		String options = event.getOptions();
		if (options == null || options.isBlank()) {
			logger.warning("Missing options ");
			return;
		}

		try {
			String key = analyticController.getOption(event.getKey(), "key", options, null);
			String value = analyticController.getOption(event.getKey(), "value", options, null);
			if (key == null || key.isBlank()) {
				logger.warning("Missing option 'key' ");
				return;
			}
			if (value == null || value.isBlank()) {
				logger.warning("Missing option 'value' ");
				return;
			}

			logger.info("├── Analyse worklist by key " + key + ":" + value);
			String description = "";
			String link = "";
			// do we have a lable defined in the options?
			String label = analyticController.getOption(event.getKey(), "label", options, value);

			// do we have a process key?
			if ("process".equals(key)) {
				// lookup process
				ItemCollection process = teamService.getProcessByName(value);
				if (process == null) {
					logger.warning("Cant resolve process by name '" + value + "'");
					return;
				}
				key = "process.ref";
				value = process.getUniqueID();
				description = process.getItemValueString("txtdescription");
				link = "/pages/workitems/worklist.xhtml?processref=" + process.getUniqueID();
				label = process.getItemValueString("name");
			}

			// Count....
			if (event.getKey().startsWith("worklist.stats.count.")) {
				String query = "(type:workitem) AND (" + key + ":\"" + value + "\")";
				long count;

				count = documentService.count(query);
				logger.info("│   ├── count=" + count);
				event.setValue(count);
				event.setLabel(label);
				event.setDescription(description);
				// compute a search link if we are in a process / workflowgroup
				ItemCollection process = modelController.findProcessByWorkflowGroup(value);
				if (process != null) {
					link = "/pages/workitems/worklist.xhtml?processref=" + process.getUniqueID() + "&workflowgroup="
							+ value;
				}
				event.setLink(link);
			}

			// Chart....
			if (event.getKey().startsWith("worklist.stats.chart.")) {
				// generate chart...
				event.setValue(buildWorkitemsChart(key, value, label + " / requests by month"));
				event.setLabel(label);
				event.setDescription(description);
			}

		} catch (QueryException e) {
			logger.severe("Failed to compute stats: " + e.getMessage());
		}

	}

	/**
	 * Computes a ChartJS JSON object with the count of workitems over the last 6
	 * months
	 * 
	 * <pre>
	 * 
	   chartCfg = {
		  type: 'bar',
		  data: {
			datasets: [{
			  data: [20, 10],
			}],
			labels: ['1', '2']
		  }
	    }
	 * 
	 * </pre>
	 * 
	 * @return
	 */
	private String buildWorkitemsChart(String key, String value, String label) {

		countWorkitemsByKey(key, value);

		// Step 2: Prepare the JSON structure and ensure we have all weeks from earliest
		// to latest
		JsonArrayBuilder dataBuilderCount = Json.createArrayBuilder();
		JsonArrayBuilder labelsBuilder = Json.createArrayBuilder();

		for (MonthStats monthStats : monthStatsList) {
			dataBuilderCount.add(monthStats.getCount());
			labelsBuilder.add(monthStats.getMonthKey());
		}

		// Step 3: Prepare the JSON structure
		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder()
				.add("type", "bar")
				.add("data", Json.createObjectBuilder()
						.add("datasets", Json.createArrayBuilder()
								.add(Json.createObjectBuilder() // Third dataset for 'total'
										.add("label", label)
										.add("backgroundColor", "#4bc0c0")
										.add("borderWidth", 1)
										.add("data", dataBuilderCount)))
						.add("labels", labelsBuilder));

		// Step 4: Return the JSON structure as a string
		JsonObject chartCfg = jsonBuilder.build();
		return chartCfg.toString();
	}

	/**
	 * computes the count of workitems per months by a given key. Possible keys are
	 * 'process.ref', '$workflowgroup'
	 * 
	 * @return
	 */
	private void countWorkitemsByKey(String key, String value) {

		logger.info("Accumulating last 6 months...");

		// Generate MonthStats for last 6 months
		monthStatsList = generateLast6Months();

		// Step 1: Group count by month
		for (MonthStats monthStats : monthStatsList) {
			try {
				// Use the MonthStats object to create the query
				String query = "(" + key + ":\"" + value
						+ "\") AND (type:\"workitem\" OR type:\"workitemarchive\") " +
						"AND ($created:[" + monthStats.startDate + "000000 TO " + monthStats.endDate + "235959])";
				long count = documentService.count(query);

				// Set count in MonthStats object
				monthStats.setCount(count);
				logger.info("Month " + monthStats.getMonthKey() +
						" (" + monthStats.getStartDate() + " to " + monthStats.getEndDate() +
						"): " + count + " workitems");
			} catch (QueryException e) {
				logger.severe("Failed to get data: " + e.getMessage());
			}

		}
	}

	/**
	 * Method that generates months starting from the first day of each month
	 * 
	 * @return List of strings representing months in format 'YYYY-MM'
	 */
	public static List<MonthStats> generateLast6Months() {
		List<MonthStats> monthStatsList = new ArrayList<>();
		YearMonth currentMonth = YearMonth.now();

		// Generate 6 months backwards from current month
		for (int i = 5; i >= 0; i--) {
			YearMonth targetMonth = currentMonth.minusMonths(i);
			MonthStats monthStats = new MonthStats(targetMonth);
			monthStatsList.add(monthStats);
		}

		return monthStatsList;
	}

	/**
	 * Holds the statistic data for a month
	 */
	static class MonthStats {
		private long count = 0;
		private String startDate; // YYYYMMDD
		private String endDate; // YYYYMMDD
		private String monthKey; // YYYY-MM

		public MonthStats(YearMonth yearMonth) {
			this.monthKey = yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));

			// Calculate first day of the month
			LocalDate firstDay = yearMonth.atDay(1);
			this.startDate = firstDay.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

			// Calculate last day of the month
			LocalDate lastDay = yearMonth.atEndOfMonth();
			this.endDate = lastDay.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		}

		public void setCount(long count) {
			this.count = count;
		}

		public long getCount() {
			return count;
		}

		public String getStartDate() {
			return startDate;
		}

		public String getEndDate() {
			return endDate;
		}

		public String getMonthKey() {
			return monthKey;
		}
	}
}
