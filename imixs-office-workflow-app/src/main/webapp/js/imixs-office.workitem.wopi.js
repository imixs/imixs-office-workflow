// Imixs-Adapters-Wopi Integration

//var wopiLastWorkflowEvent; // stores the last workfow UI action button
var wopiConfirmMessage;
/**
 * Init Method for the wop document integration
 * 
 *  - we verify if a wopo link has the tag 'data-wopi-openonload=true'. In this case we trigger the link
 *    to automatically load the wopi editor. 
 */
$(document).ready(function() {
	// if an wopi-editor-link has the tag data-wopi-openonload=true than we click the link on load....
	$('a[data-wopi-openonload="true"]').click();
});


// open the wopi viewer
function openWopiViewer(url,filename, discardMessage) {
	
	// do we have unsaved changes?
	// if document was modifed without save then ask the user....
	if (imixsWopi.isModified) {
		if (!confirm(discardMessage)) {
			// cancel operation!
			return false;
		} 
	}

	// minimize Document Preview (if open )
	//imixsOfficeWorkitem.closeDocumentPreview();
	
	$('#wopi_header_filename_id').html(filename);
	// hide the workflow form
	$('#imixs_workitem_form_id').hide();
	// open viewer...	
	$('#wopi_controlls').show();	
	imixsWopi.openViewer('wopi_canvas', url, filename);
	

}



function workitemSaveCallback(action) {
	if (imixsWopi.isModified) {
		return confirm(wopiConfirmMessage);
	}
}

// close the wopi viewer
function closeWopiViewer(confirmMessage) {
	
	// if document was modifed without save then ask the user....
	if (imixsWopi.isModified) {
		if (!confirm(confirmMessage)) {
			// cancel operation!
			return false;
		} 
		imixsWopi.isModified=false;
	}
	
	if ($('#wopi_controlls').is(":visible")) {
		//console.log("close ");
		$('#wopi_controlls').hide();
		imixsWopi.closeViewer();
		// show workflow form
		$('#imixs_workitem_form_id').show();
	}
}





