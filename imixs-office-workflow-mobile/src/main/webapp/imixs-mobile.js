/*! Imixs Office Workflow - mobile v0.0.1 */

var officeBaseURL = "/office/";

// Bind mobileinit event
$(document).bind("mobileinit", function() {

	setupWorkflowService("http://localhost:8080/office-rest/", "", 1);
	// alert('binding..');
	// overwrite configuration if necessarry
	$.extend($.mobile, {
	// config...

	});

	$(document).on('pagecreate', '#worklist', function(event, ui) {
		// pagecreate pageinit pageshow
		// alert('pagecreate bind refresh method');
		$(document).off('refreshWorklist', '#worklist', refreshWorklistUI);
		$(document).on('refreshWorklist', '#worklist', refreshWorklistUI);
	});

	// Event Handler for process page on pageshow event
	// pageshow pageload
	$(document).on('pageshow', '#worklist', function(event, ui) {
		loadWorkitems('workflow/worklistbyowner/null.json', '#worklist');
	});

});

// ########### Worklist ###################

// this method clears the worklist from the local cache and
// removes the view body
function clearWorklist(view_type) {
	// alert('clearView....');
	localStorage.removeItem("com.imixs.workflowgroups." + view_type);
	$("#worklist_view").empty();

}

/**
 * This method is triggered by the loadWorkitems method and refreshes the
 * worklist output
 * 
 * @param e
 * @param adata
 * @param aservice
 */
function refreshWorklistUI(e, data, aservice) {
	$("#worklist_view").empty();
	// iterate over all workitems
	$.each(data.entity, function(i, workitem) {

		var modified = getEntityItemValue(workitem, '$modified');
		var summary = getEntityItemValue(workitem, 'txtworkflowsummary');
		var imageURL = officeBaseURL
				+ getEntityItemValue(workitem, 'txtworkflowimageurl');
		var group = getEntityItemValue(workitem, 'txtworkflowgroup');
		var status = getEntityItemValue(workitem, 'txtworkflowstatus');
		var id = getEntityItemValue(workitem, '$uniqueid');
		var img = '<img class="ui-li-icon imixs-image ui-li-thumb" src="' + imageURL
				+ '" />';

		// $("#worklist_view").append(
		// "<li>" + "<a href=\"workitem.html?id=" + id + "\">" +
		// img
		// + summary +'</a><p class="ui-li-desc">'+ group
		// +status +"</p></li>");

		$("#worklist_view").append(
				'<li>'

				+ '<a href="workitem.html?id=' + id + '">' + img + '<h3>'
						+ summary + '</h3><p>' + '<b>' + group + '</b>: '
						+ status + '</p><p class="ui-li-aside">Last update: '
						+ modified + '</p></a></li>');

	});

	// Update the layout
	$("#worklist_view").listview("refresh");

}
