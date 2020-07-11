// workitem scripts

IMIXS.namespace("com.imixs.workflow.office.workitem");

var months = [ "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug",
	"Sep", "Okt", "Nov", "Dec" ];


var documentPreview;
var documentPreviewIframe;

/**
 * Init Method for the workitem page
 * 
 *  - set history nav
 * 
 */
$(document).ready(function() {
	
	$('.imixs-subnav a').button({
		icons : {
			secondary : "ui-icon-plus"
		},
		text : true
	});
	
	printWorkitemReferences();
	
	// get chronicle status from cookie
	var c_value = document.cookie;
	imixsOfficeWorkflow.imixs_document=true; // default
	if (c_value.indexOf("imixs.office.document=")>-1) {
		// read cookie data
		imixsOfficeWorkflow.imixs_document=c_value.indexOf("imixs.office.document=true")>-1;
	}
	imixsOfficeWorkflow.imixs_chronicle_comments=true;
	imixsOfficeWorkflow.imixs_chronicle_nav=JSON.parse('{ "comment" : true, "files":true, "version":true, "reference":true }'); 
	
	
	// init...
	hideComments(null);
	
	// hide documents-file-deeplink
	$("#documents-file-deeplink").hide();
	
	
	// update the link action for each file
	// we redirect the href into the iframe target
	$("[id$='dmslist'] .file-open-link").each(
		function(index, element) {						
			$(this).click(function(){
				var file_link=$(this).attr('href');
				//updateIframe(file_link);
					
				showDocument($(this).text(),file_link);
				
				// click into document tab if resolution <1800
				if (window.innerWidth<1800) {
					$(".chronicle-tab-documents").click();
				}
				// cancel link
			    return false;
			});
		});
		
		
	// we redirect the href from chronicle into the iframe target
	$(".files a.attachmentlink").each(
		function(index, element) {						
			$(this).click(function(){
				var file_link=$(this).attr('href');
				//updateIframe(file_link);
				showDocument($(this).text(),file_link);
				
				// click into document tab if resolution <1800
				if (window.innerWidth<1800) {
					$(".chronicle-tab-documents").click();
				}
				// cancel link
			    return false;
			});
		});
		
		
	// set the default preview frame
	documentPreviewIframe=document.getElementById('imixs_document_iframe_embedded');
		
	// autoload first pdf into preview if available.... 
	autoPreviewPDF();
	
});

/*
 * This callback method is triggered by the imxs-faces.js file upload
 * component. The method updates the deep links for uploaded files
 */
function onFileUploadChange() {
	
	$(".imixsFileUpload_uploaded_file a").each(
	function(index, element) {						
		$(this).click(function(){
			var file_link=$(this).attr('href');
			//updateIframe(file_link);
			showDocument($(this).text(),file_link);
			
			var windowWidth = window.innerWidth;
			if (windowWidth>=1800) {
				maximizeDocumentPreview();
			}
			// cancel link
		    return false;
		});
	});
	
	/* autoload first pdf into preview if available.... */
	$(".imixsFileUpload_uploaded_file a").each(
	function(index, element) {						
		var attachmentName=$(this).text();
			if (attachmentName.endsWith('.pdf') || attachmentName.endsWith('.PDF')) {
				$(this).click();
				return false;
			}
		
	});
}
/*
 * This method loads the first pdf and starts a autopreview
 */
function autoPreviewPDF() {
	$("[id$='dmslist'] .file-open-link").each(
		function(index, element) {
			var attachmentName=$(this).text();
			if (attachmentName.endsWith('.pdf') || attachmentName.endsWith('.PDF')) {									
				$(this).click();
				var windowWidth = window.innerWidth;
				if (windowWidth>=1800) {
					maximizeDocumentPreview();
				}
				return false;
			}
		});
}

function hideComments(event) {
	$('.dms-comment-panel').hide();
}

/*
 * This method hides the document preiview window
 * and shows the history tab.
 */
function closeDocumentPreview() {
	
	minimizeDocumentPreview();
	// switch to chronicle
	$(".chronicle-tab-history").click();
}

/*
 * The method hides the document preiview window and opens 
 * the document in the minimized preview on the documents tab
 */
function minimizeDocumentPreview() {
	$('.imixs-workitem-form').css('width','calc(66.6666% - 0px)');
	$('.imixs-workitem-document').hide();
	$('.imixs-workitem-document-embedded').show();

	// set preview cookie
	document.cookie = "imixs.office.document.preview=false";
	documentPreview=$('.imixs-workitem-document-embedded');

	// update iframe
	var currentURL="";
	if (documentPreviewIframe) {
		currentURL=documentPreviewIframe.src;
	}
	documentPreviewIframe=document.getElementById('imixs_document_iframe_embedded');
	documentPreviewIframe.src=currentURL;
	
	$(".chronicle-tab-documents").click();
	
}



/*
 * The method shows the document preiview window
 */
function maximizeDocumentPreview() {
	$('.imixs-workitem-form').css('width','calc(33.333% - 0px)');
	$('.imixs-workitem-document').show();
	$('.imixs-workitem-document-embedded').hide();
	
	// set preview cookie
	document.cookie = "imixs.office.document.preview=false";
	documentPreview=$('.imixs-workitem-document');
	
	// update iframe
	var currentURL="";
	if (documentPreviewIframe) {
		currentURL=documentPreviewIframe.src;
	}
	documentPreviewIframe=document.getElementById('imixs_document_iframe');
	documentPreviewIframe.src=currentURL;
	
	
}



/*
 * A document loads the current document (link) into the documentPreviewIframe
 * and displays the document title.
 
 */
function showDocument(title, link) {
	// cut title if length >64 chars
	if (title.length>64) {
		title=title.substring(0,64)+"...";
	}
	$('.document-title',documentPreview).text(title);
	documentPreviewIframe.src=link;
	// update deeplink
	$('.document-deeplink',documentPreview).attr('href',link);
}


/**
 * Wokritem References - print year/month sections
 * 
 */
function printWorkitemReferences() {
	var lastDatemark = 999999;
	var lastYear = 9999;
	entries = $(".imixs-timeline-entry");
	tabel = $("#timeline-table");
	// first  we add table rows for each month.....
	$.each(entries,function(i, val) {
		var currentDatemark = parseInt($(
				this).find(".datemark")
				.text());

		var currentYear = parseInt($(
				this).find(".datemark")
				.text().substring(0, 4));
		var currentMonth = parseInt($(
				this).find(".datemark")
				.text().substring(4, 6));
		if (currentDatemark < lastDatemark) {
			// append new table row for each new year and new month....
			if (currentYear < lastYear) {
				$('#timeline-table tr:last')
						.after(
								"<tr class='imixs-timeline-year'><td></td><td style='text-align:center;'><h1>"
										+ currentYear
										+ "</h1></td><td></td></tr>");
				lastYear = currentYear;
			}
			$('#timeline-table tr:last')
					.after(
							"<tr><td id='" + currentDatemark +"-out'></td><td style='text-align:center;'><h2>"
									+ months[currentMonth - 1]
									+ "</h2></td><td id='" + currentDatemark +"-in'></td></tr>");
		}
		lastDatemark = currentDatemark;
	});

	// now we can sort each element into the corresponding cell...
	$.each(entries, function(i, val) {
		var currentDatemark = parseInt($(this).find(
				".datemark").text());
		var newcell;
		if ($(this).hasClass("workitemref-in"))
			newcell = $("#" + currentDatemark + "-in",
					tabel);
		else
			newcell = $("#" + currentDatemark + "-out",
					tabel);
		if (newcell)
			$(newcell).append($(this));
	});

}

function printQRCode() {
	fenster = window
			.open(
					imixsOfficeWorkflow.contextPath+"/pages/workitems/qrcode_print.jsf?id="+imixsOfficeWorkflow.workitem_uid,
					"message.print",
					"width=760,height=300,status=no,scrollbars=no,resizable=yes");
	fenster.focus();
}