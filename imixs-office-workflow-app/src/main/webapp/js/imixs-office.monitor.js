"use strict";

// workitem scripts

IMIXS.namespace("org.imixs.workflow.monitor");

var mainCharts;		 	// array of all visible main charts
var groupData;  		// all datasets for each group
var filter; 			// optional filter
var processRef; 	    // holding the current process ref - set form the monitor page
var detailChartOwner;
var detailChartSpace;
var currentWorkflowGroupSelection;
var currentWorkflowStatusSelection;


/**
 * Init Method for the workitem page
 * 
 *  - set history nav
 * 
 */
$(document).ready(function () {


	imixsOfficeMonitor.initMainWorkflowCharts();

	// find all imxis-report portlets and start to load the report data..
	var allReports = $(".imixs-reports .imixs-header");
	$(allReports).each(function (index) {
		var reportname = this.id;
		// we use the internal browser cache to avoid unecessary ajax calls 
		imixsOfficeMonitor.loadReportData(reportname, false);
	});


});




// define core module
IMIXS.org.imixs.workflow.monitor = (function () {
	if (!IMIXS.org.imixs.core) {
		console.error("ERROR - missing dependency: imixs-core.js");
	}

	var imixs = IMIXS.org.imixs.core,


		/** MONITORING CHARTS  **/


		// Helper Method to destroy all existing charts
		// used to overwrite content
		destoryCharts = function () {

			if (mainCharts) {
				mainCharts.forEach((achart) => {
					if (achart instanceof Chart) {
						achart.destroy();
					}
				});
			}
			if (detailChartOwner instanceof Chart) {
				detailChartOwner.destroy();
			}
			if (detailChartSpace instanceof Chart) {
				detailChartSpace.destroy();
			}
			// show details secition
			document.getElementById('monitoring-details-section-id').style.display = "none";
			$('.monitoring-table').remove();
			$(".monitoring-datatable").remove();
		},

		// This mehtod generate Chart objects for each WorkflowGroup.
		// Each chart registers an onClick listener to react on clicks 
		// loading the details Charts.
		initMainWorkflowCharts = function () {

			// destroy existing main charts if availalbe
			destoryCharts();

			console.log("init main workflow charts....")
			mainCharts = [];
			for (let chart of imixsOfficeMonitor.groupData) {
				// console.log("chart id=" + chart.id + " group="+chart.name);
				var ctx = document.getElementById(chart.id).getContext('2d');

				if (ctx) {
					document.getElementById(chart.id).style.display = "block";
					// generate a new chart object
					var mainChart = new Chart(ctx, {
						type: 'doughnut',
						data: chart.data,
						options: {
							responsive: false,
							maintainAspectRatio: true,
							plugins: {
								legend: {
									display: true,
									position: 'left'
								}
							},
							onClick: function (e, activeEls) {
								// extract the event data and compute selected element data
								let datasetIndex = activeEls[0].datasetIndex;
								let dataIndex = activeEls[0].index;
								imixsOfficeMonitor.currentWorkflowGroupSelection = e.chart.data.datasets[datasetIndex].label;
								imixsOfficeMonitor.currentWorkflowStatusSelection = e.chart.data.labels[dataIndex];
								//console.log("In click", datasetLabel, label, value,imixsOfficeMonitor.processRef );
								imixsOfficeMonitor.loadDetailChartByOwner();
								imixsOfficeMonitor.loadDetailChartBySpace();
							}
						}
					});
					mainChart.resize();
					mainCharts.push(mainChart);

				}
			}

		},



		// This mehtod generate Table views for each WorkflowGroup.
		// Each view registers an onClick listener to react on clicks 
		// loading the details Charts.
		initMainWorkflowTableView = function () {
			// destroy existing main charts if availalbe
			destoryCharts();

			console.log("init main workflow charts....")
			mainCharts = [];
			for (let chart of imixsOfficeMonitor.groupData) {
				var ctx = document.getElementById(chart.id);
				if (ctx) {
					$(ctx).hide();
					// generate a new table view
					//ctx.
					var content = "<table class='monitoring-datatable'>";
					content += '<tr><th>Status</th><th></th></tr>';
					for (var i = 0; i < chart.data.labels.length; i++) {

						var jsclick = "imixsOfficeMonitor.currentWorkflowGroupSelection = '" + chart.data.datasets[0].label + "';imixsOfficeMonitor.currentWorkflowStatusSelection = '" + chart.data.labels[i] + "';imixsOfficeMonitor.loadDetailTableByOwner();imixsOfficeMonitor.loadDetailTableBySpace();"
						content += '<tr><td class="imixs-viewentry-main-link"><a href="javascript:' + jsclick + '">' + chart.data.labels[i] + '</a></td>';
						content += '<td style="text-align:center;">' + chart.data.datasets[0].data[i] + '</td></tr>';
					}
					content += "</table>"
					$(ctx).parent().append(content);

				}
			}

		},


		// THis method starts an ajax request to load the details for a specific
		// workflowgroup/task grouped by Owner
		loadDetailChartByOwner = function () {
			//console.log("...loadDetailChartByOwner for " + workflowgroup + "/" + task);
			// show details secition
			document.getElementById('monitoring-details-section-id').style.display = "block";
			// update title
			document.getElementById('monitoring-details-title-id').innerHTML = imixsOfficeMonitor.currentWorkflowGroupSelection + " / " + imixsOfficeMonitor.currentWorkflowStatusSelection;
			// load chart
			$.ajax({
				url: "/api/monitor/" + imixsOfficeMonitor.processRef + "/" + imixsOfficeMonitor.currentWorkflowGroupSelection + "/" + imixsOfficeMonitor.currentWorkflowStatusSelection + "/$owner?filter=" + imixsOfficeMonitor.filter,
				type: "GET",
				//contentType: "application/json;charset=utf-8",
				dataType: "json"
			}).done(
				function (data) {
					//console.log(" we got the data - generating chart....");
					// clear old content
					if (detailChartOwner instanceof Chart) {
						detailChartOwner.destroy();
					}
					// construct a new doughnut chart in the details pane
					var ctx = document.getElementById("monitoring-details-owner-pane-id").getContext('2d');
					if (ctx) {
						document.getElementById("monitoring-details-owner-pane-id").style.display = "block";
						// generate a new chart object
						detailChartOwner = new Chart(ctx, {
							type: 'doughnut',
							data: data,
							options: {
								responsive: false,
								maintainAspectRatio: true,
								plugins: {
									legend: {
										display: true,
										position: 'left'
									}
								},
								onClick: function (e, activeEls) {
									// extract the event data and compute selected element data
									let dataIndex = activeEls[0].index;
									let label = e.chart.data.labels[dataIndex];
									// we store the userid in a extra value within the data set called 'origins'
									let userid = e.chart.data.origins[dataIndex];
									console.log("search click",
										imixsOfficeMonitor.currentWorkflowGroupSelection,
										imixsOfficeMonitor.currentWorkflowStatusSelection,
										userid, imixsOfficeMonitor.processRef);
									let url = "./workitems/worklist.xhtml?processref=" + imixsOfficeMonitor.processRef
										+ "&owner=" + userid
										+ "&workflowgroup=" + imixsOfficeMonitor.currentWorkflowGroupSelection
										+ "&task=" + imixsOfficeMonitor.currentWorkflowStatusSelection;
									document.location.href = url;
								}
							}
						});
						detailChartOwner.resize();
					}

				});

		},



		// THis method starts an ajax request to load the details for a specific
		// workflowgroup/task grouped by Owner
		loadDetailChartBySpace = function () {

			//console.log("...loadDetailChartBySpace for " + workflowgroup + "/" + task);
			// show details secition
			document.getElementById('monitoring-details-section-id').style.display = "block";
			// load chart
			$.ajax({
				url: "/api/monitor/" + imixsOfficeMonitor.processRef + "/" + imixsOfficeMonitor.currentWorkflowGroupSelection + "/" + imixsOfficeMonitor.currentWorkflowStatusSelection + "/space.ref?filter=" + imixsOfficeMonitor.filter,
				type: "GET",
				//contentType: "application/json;charset=utf-8",
				dataType: "json"
			}).done(
				function (data) {
					//console.log(" we got the data - generating chart....");
					// clear old content
					if (detailChartSpace instanceof Chart) {
						detailChartSpace.destroy();
					}
					// construct a new doughnut chart in the details pane
					var ctx = document.getElementById("monitoring-details-space-pane-id").getContext('2d');
					if (ctx) {
						document.getElementById("monitoring-details-space-pane-id").style.display = "block";
						// generate a new chart object
						detailChartSpace = new Chart(ctx, {
							type: 'doughnut',
							data: data,
							options: {
								responsive: false,
								maintainAspectRatio: true,
								plugins: {
									legend: {
										display: true,
										position: 'left'
									}
								},
								onClick: function (e, activeEls) {
									// extract the event data and compute selected element data
									let dataIndex = activeEls[0].index;
									let label = e.chart.data.labels[dataIndex];
									// we store the userid in a extra value within the data set called 'origins'
									let spaceid = e.chart.data.origins[dataIndex];
									console.log("search click",
										imixsOfficeMonitor.currentWorkflowGroupSelection,
										imixsOfficeMonitor.currentWorkflowStatusSelection,
										label, spaceid, imixsOfficeMonitor.processRef);
									let url = "./workitems/worklist.xhtml?processref=" + imixsOfficeMonitor.processRef
										+ "&spaceref=" + spaceid
										+ "&workflowgroup=" + imixsOfficeMonitor.currentWorkflowGroupSelection
										+ "&task=" + imixsOfficeMonitor.currentWorkflowStatusSelection;
									document.location.href = url;
								}
							}
						});
						detailChartSpace.resize();
					}


				});

		},


		// THis method starts an ajax request to load the details for a specific
		// workflowgroup/task grouped by Owner
		loadDetailTableByOwner = function () {
			$(".monitoring-datatable-owner").remove();
			//console.log("...loadDetailChartByOwner for " + workflowgroup + "/" + task);
			// show details secition
			document.getElementById('monitoring-details-section-id').style.display = "block";
			// update title
			document.getElementById('monitoring-details-title-id').innerHTML = imixsOfficeMonitor.currentWorkflowGroupSelection + " / " + imixsOfficeMonitor.currentWorkflowStatusSelection;
			// load chart
			$.ajax({
				url: "/api/monitor/" + imixsOfficeMonitor.processRef + "/" + imixsOfficeMonitor.currentWorkflowGroupSelection + "/" + imixsOfficeMonitor.currentWorkflowStatusSelection + "/$owner?filter=" + imixsOfficeMonitor.filter,
				type: "GET",
				//contentType: "application/json;charset=utf-8",
				dataType: "json"
			}).done(
				function (data) {
					// construct a new table in the details pane
					var ctx = document.getElementById("monitoring-details-owner-pane-id");
					$(ctx).hide();
					// generate a new table view
					var content = "<table class='monitoring-datatable monitoring-datatable-owner'>";
					content += '<tr><th>Status</th><th></th></tr>';
					for (var i = 0; i < data.labels.length; i++) {
						content += '<tr><td class="imixs-viewentry-main-link"><a href="' + "./workitems/worklist.xhtml?processref=" + imixsOfficeMonitor.processRef + "&owner=" + data.origins[i] + "&workflowgroup=" + imixsOfficeMonitor.currentWorkflowGroupSelection + "&task=" + imixsOfficeMonitor.currentWorkflowStatusSelection + '">' + data.labels[i] + '</a></td>';
						content += '<td style="text-align:center;">' + data.datasets[0].data[i] + '</td></tr>';
					}
					content += "</table>"
					$(ctx).parent().append(content);
				});
		},



		// THis method starts an ajax request to load the details for a specific
		// workflowgroup/task grouped by Owner
		loadDetailTableBySpace = function () {
			$(".monitoring-datatable-space").remove();
			//console.log("...loadDetailChartByOwner for " + workflowgroup + "/" + task);
			// show details secition
			document.getElementById('monitoring-details-section-id').style.display = "block";
			// update title
			document.getElementById('monitoring-details-title-id').innerHTML = imixsOfficeMonitor.currentWorkflowGroupSelection + " / " + imixsOfficeMonitor.currentWorkflowStatusSelection;
			// load chart
			$.ajax({
				url: "/api/monitor/" + imixsOfficeMonitor.processRef + "/" + imixsOfficeMonitor.currentWorkflowGroupSelection + "/" + imixsOfficeMonitor.currentWorkflowStatusSelection + "/space.ref?filter=" + imixsOfficeMonitor.filter,
				type: "GET",
				//contentType: "application/json;charset=utf-8",
				dataType: "json"
			}).done(
				function (data) {
					// construct a new table in the details pane
					var ctx = document.getElementById("monitoring-details-space-pane-id");
					$(ctx).hide();
					// generate a new table view
					var content = "<table class='monitoring-datatable monitoring-datatable-space'>";
					content += '<tr><th>Status</th><th></th></tr>';
					for (var i = 0; i < data.labels.length; i++) {
						content += '<tr><td class="imixs-viewentry-main-link"><a href="' + "./workitems/worklist.xhtml?processref=" + imixsOfficeMonitor.processRef + "&spaceref=" + data.origins[i] + "&workflowgroup=" + imixsOfficeMonitor.currentWorkflowGroupSelection + "&task=" + imixsOfficeMonitor.currentWorkflowStatusSelection + '">' + data.labels[i] + '</a></td>';
						content += '<td style="text-align:center;">' + data.datasets[0].data[i] + '</td></tr>';
					}
					content += "</table>"
					$(ctx).parent().append(content);
				});
		},


		/** CUSTOM REPORTs  **/

		// This method loads a report and refreshes the corresponding chart diagramm.
		// We use the browser local storage to cache reports
		// The boolean 'refresh==true' indicates if the current cache should be discarded.
		loadReportData = function (reportid, refresh) {
			if (!refresh) {
				var storedDataObject = getStoredDataObject();
				var chartData = null;
				$.each(storedDataObject.report, function (index, value) {
					// search chartdate entry with same id...
					if (value.id == reportid) {
						// found!
						chartData = value.data;
						return false;
					}
				});

				if (chartData) {
					refreshChartDiagramm(reportid, chartData);
					// finish!
					return false;
				}
			}

			//console.log("loading chart data '" + reportid + "'...'");
			// get report name with .imixs-report extension form the reportid...
			var report = reportid + ".imixs-report";
			var request_url = "/api/report/" + report + "?pageSize=9999";
			// We replace the sufix .imixs-report with -imixs-report so that we an use the report name as an ID
			$.ajax({
				url: request_url,
				type: "GET",
				//contentType: "application/json;charset=utf-8",
				dataType: "json"
			}).done(
				function (data) {
					// retrieve data object
					var storedDataObject = getStoredDataObject();

					// update array or push new data object if not available...
					var found = false;
					$.each(storedDataObject.report, function (index, value) {
						// search chartdate entry with same id...
						if (value.id == reportid) {
							// found!
							//console.log("update chart data in local cache...");
							storedDataObject.report[index].data = data;
							found = true;
							return false;
						}
					});
					if (!found) {
						// push the chart data object together with its id into the storedDadaObject...
						//console.log("pushing chart data into local cache....");
						storedDataObject.report.push({ id: reportid, data: data });
					}

					sessionStorage.setItem("imixs-office-wrokflow.report.data", JSON.stringify(storedDataObject));
					refreshChartDiagramm(reportid, data);
				});
		},


		getStoredDataObject = function () {
			var storedDataObject = null;
			if (typeof (Storage) !== "undefined") {
				// retrieve data object from localStorage/sessionStorage.
				var dataString = sessionStorage.getItem("imixs-office-wrokflow.report.data");
				try {
					storedDataObject = JSON.parse(dataString);
				} catch (e) {
					storedDataObject = null;
				}
				// if the storedDataObject is undefined or not initalized....    
				if (!storedDataObject || !storedDataObject.report) {
					// ...then initialize the storage-object with an empty dummy array..
					storedDataObject = { date: "", report: new Array() };
				}
			}
			return storedDataObject;
		},


		// This method rebuilds a chart diagramm
		refreshChartDiagramm = function (reportid, data) {
			//console.log("building chart.js diagramm '" + reportid + "'...");
			// update h1 with label
			$("#" + reportid + " h1").text(data.title);

			// clear old data (http://stackoverflow.com/questions/24815851/how-to-clear-a-chart-from-a-canvas-so-that-hover-events-cannot-be-triggered#25064035)
			$('#canvas-' + reportid).remove(); // this is my <canvas> element
			$("#canvas-container-" + reportid).append("<canvas id=\"canvas-" + reportid + "\"></canvas>");

			var ctx = document.getElementById("canvas-" + reportid)
				.getContext("2d");

			if (data.type) {
				// line chart
				window.report1 = new Chart(ctx, {
					type: data.type,
					data: data,
					options: data.options
				});
			} else {
				alert('chart type for report ' + data.datasets[0].label + ' is undefined!');
			}

		};




	// public API
	return {
		initMainWorkflowCharts: initMainWorkflowCharts,
		initMainWorkflowTableView: initMainWorkflowTableView,
		loadDetailChartByOwner: loadDetailChartByOwner,
		loadDetailChartBySpace: loadDetailChartBySpace,
		loadDetailTableByOwner: loadDetailTableByOwner,
		loadDetailTableBySpace: loadDetailTableBySpace,
		loadReportData: loadReportData,
		refreshChartDiagramm: refreshChartDiagramm,
		getStoredDataObject: getStoredDataObject
	};

}());

// Define public namespace
var imixsOfficeMonitor = IMIXS.org.imixs.workflow.monitor;


