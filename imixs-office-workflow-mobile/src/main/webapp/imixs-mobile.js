/*! Imixs Office Workflow - mobile v0.0.1 */

var officeBaseURL = "/office/";
var serviceURL="";
var lastServiceURL="";

// Bind mobileinit event
$(document).bind("mobileinit", function() {

	// compute base url
	var host=window.location.href;
	var pos=host.indexOf('/',7);
	host=host.substring(0,pos+1);
	//alert(host);
	setupWorkflowService(host+"office-rest/", "", 1);
	// alert('binding..');
	// overwrite configuration if necessarry
	$.extend($.mobile, {
	// config...

	});
	
	
	// swipe right event -> history back
	$(document).on('swiperight', function(event, ui) {
		 history.back();
	});
	
	$(document).on('swipeleft', function(event, ui) {
		 history.forward();
	});
	
	

	// Register refreshWorklistUI method..
	$(document).on('pagecreate', '#worklist', function(event, ui) {
		// pagecreate pageinit pageshow
		// alert('pagecreate bind refresh method');
		$(document).off('refreshWorklist', '#worklist', refreshWorklistUI);
		$(document).on('refreshWorklist', '#worklist', refreshWorklistUI);
	});

	// Event Handler for process page on pageshow event
	// pageshow pageload
	$(document).on('pageshow', '#worklist', function(event, ui) {
		// clear worklist content if url changed!
		if (lastServiceURL!=serviceURL) {
			$("#worklist_view").empty();
			//alert(serviceURL);
		}
		lastServiceURL=serviceURL;
		loadWorkitems(serviceURL, '#worklist');
	});

	
	// Register refreshWorkitemUI
	$(document).on('pagecreate', '#workitem', function(event, ui) {
		$(document).off('afterRefresh', '#workitem', refreshWorkitemUI);
		$(document).on('afterRefresh', '#workitem', refreshWorkitemUI);
	});
	
	// Event Handler for  pageshow event - loadWorkitem...
	$(document).on('pageshow', '#workitem', function(event, ui) {
		var id=$.urlParam('id');
		//alert(id);
		loadWorkitem(id, '#workitem');
	});
	
});


/**
 * This method updates the Wrokitem UI
 * @param e
 * @param workitem
 */
function refreshWorkitemUI(e, workitem) {

	$("#workitem_summary").text(getEntityItemValue(workitem, "txtworkflowsummary"));
	$("#workitem_abstract").html(getEntityItemValue(workitem, "txtworkflowabstract"));

	$("#workitem_workflowstatus").text(getEntityItemValue(workitem, "txtworkflowstatus"));
	$("#workitem_workflowgroup").text(getEntityItemValue(workitem, "txtworkflowgroup"));
	$("#workitem_currenteditor").text(getEntityItemValue(workitem, "namcreator"));
	$("#workitem_modified").text(getEntityItemValue(workitem, "$modified"));
	$("#workitem_process").text(getEntityItemValue(workitem, "txtprocessname"));
        
	var history=getEntityItemValueArray(workitem,"txtworkflowhistorylog");
	
}

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

		$("#worklist_view").append(
				'<li>'

				+ '<a href="workitem.html?id=' + id + '" data-transition="slide">' + img + '<h3>'
						+ summary + '</h3><p>' + '<b>' + group + '</b>: '
						+ status + '</p><p class="ui-li-aside">Last update: '
						+ modified + '</p></a></li>');

	});

	// Update the layout
	$("#worklist_view").listview("refresh");

}


/**
 * Get a query param value
 */
$.urlParam = function(name){
    var results = new RegExp('[\\?&]' + name + '=([^&#]*)').exec(window.location.href);
    if (results==null){
       return null;
    }
    else{
       return results[1] || 0;
    }
};
