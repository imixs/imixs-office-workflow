// Imixs-Adapters-Wopi Integration


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
function openWopiViewer(url,filename) {
	$('#wopi_header_filename_id').html(filename);
	// hide the workflow form
	$('#imixs_workitem_form_id').hide();
	// open viewer...	
	$('#wopi_controlls').show();	
	imixsWopi.openViewer('wopi_canvas', url, filename);
	// define save callback for close
	imixsWopi.saveCallback = uiSaveCallback;

}

function uiSaveCallback(filename) {
	// we can do a ui update based on the filename
	// ....
	
	closeWopiViewer();
}

// close the wopi viewer
function closeWopiViewer(confirmMessage) {
	// if document was modifed without save then ask the user....
	if (imixsWopi.isModified) {
		if (confirm(confirmMessage)) {
			imixsWopi.save(); return false;
		}
	}
	console.log("close ");
	$('#wopi_controlls').hide();
	imixsWopi.closeViewer();
	// show workflow form
	$('#imixs_workitem_form_id').show();
}