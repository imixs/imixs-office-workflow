// workitem scripts

IMIXS.namespace("com.imixs.workflow.office.worklist");

/**
 * Init Method for the search view
 * 
 *  - 
 * 
 * @returns
 */
$(document).ready(function() {
	// set navigation
	document.cookie = "imixs.office.navigation=/pages/workitems/worklist.jsf;path=" + imixsOfficeWorkflow.contextPath+ "/";
	// auto focus
	focusSearchPhrase();
	
});




// set the focus on the search phrase after a ajax event
function focusSearchPhrase() {
	var phrase = $("[data-id='input_phrase']").val();        
	$("[data-id='input_phrase']").focus().val('').val(phrase);
}


//ajax refresh...
function updateSearchForm(data) {
	if (data.status === 'success') {
		imixsOfficeWorkflow.layoutAjaxEvent(data);
		// auto focus
		focusSearchPhrase();
	}
}
