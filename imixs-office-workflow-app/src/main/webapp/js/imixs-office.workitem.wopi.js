// Imixs-Adapters-Wopi Integration

//var wopiLastWorkflowEvent; // stores the last workfow UI action button
var restoreDocumentPreview=false;
var nextwopifile, nextwopiurl;
var dialogActionConfirm="Confirm", dialogActionCancel="Cancel";

/**
 * Init Method for the wop document integration
 *  - register modal dialog to confirm unsaved changes
 *  - verify if a wopi link has the tag 'data-wopi-openonload=true'. In this case we trigger the link
 *    to automatically load the wopi editor. 
 */
$(document).ready(function() {
	
	// init save confirm dialog
	$( "#wopi-save-dialog" ).dialog({
      resizable: false,
      autoOpen: false,
      height: "auto",
      width: 620,
      modal: true,
      buttons: [
		{
            text: dialogActionCancel,
            click: function () {
                $( this ).dialog( "close" );
            }
        }, {
            text: dialogActionConfirm,
            click: function () {
            	// close viewer
				$( this ).dialog( "close" );
	   			imixsWopi.isModified=false;
				if (nextwopiurl) {	 	
					openWopiViewer(nextwopiurl,nextwopifile);
				} else {
        			closeWopiViewer();
				}    
            }
        }],
    });

	// if an wopi-editor-link has the tag data-wopi-openonload=true than we click the link on load....
	$('a[data-wopi-openonload="true"]').click();

});

 
// open the wopi viewer
function openWopiViewer(url,filename, discardMessage) {
	
	// do we have unsaved changes?
	// if document was modifed without save then ask the user....
	if (imixsWopi.isModified) {
		$("#wopi-save-dialog").dialog("open");
		nextwopifile=filename;
		nextwopiurl=url;		
		return false;
	}
 
	// minimize Document Preview (if open )
	if ($('.imixs-workitem-form .imixs-document').hasClass('split')) {
		imixsOfficeWorkitem.closeDocumentPreview();
		restoreDocumentPreview=true;
	}
	
	nextwopifile=null;
	nextwopiurl=null;
	$('#wopi_header_filename_id').html(filename);
	// hide the workflow form
	$('#imixs_workitem_form_id').hide();
	// open viewer...	
	$('#wopi_controlls').show();	
	imixsWopi.openViewer('wopi_canvas', url, filename);
	

}


// This callback method is called on a workflow action
function workitemSaveCallback(action) {
	if (imixsWopi.isModified) {
		$("#wopi-save-dialog").dialog("open");
		return false;
	}
}

// close the wopi viewer
function closeWopiViewer() {
	
	// if document was modifed without save then ask the user....
	if (imixsWopi.isModified) {
		$("#wopi-save-dialog").dialog("open");
		return false;
	}
	nextwopifile=null;
	nextwopiurl=null;
	if ($('#wopi_controlls').is(":visible")) {
		//console.log("close ");
		$('#wopi_controlls').hide();
		imixsWopi.closeViewer();
		// show workflow form
		$('#imixs_workitem_form_id').show();
		
		if (restoreDocumentPreview==true) {
			imixsOfficeWorkitem.maximizeDocumentPreview();
			restoreDocumentPreview=false;
		}
	
	}
}





