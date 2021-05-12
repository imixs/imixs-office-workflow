// Imixs-Adapters-Wopi Integration

// open the wopi viewer
function openWopiViewer(url,filename) {
	$('#wopi_header_filename_id').html(filename);
	// hide the workflow form
	$('#imixs_workitem_form_id').hide();
	// open viewer...	
	$('#wopi_controlls').show();	
	imixsWopi.openViewer('wopi_canvas', url);
	// define save callback for close
	imixsWopi.saveCallback = closeWopiViewer;

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