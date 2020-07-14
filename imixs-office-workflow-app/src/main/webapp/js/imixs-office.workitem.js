// workitem scripts

IMIXS.namespace("com.imixs.workflow.office.workitem");

var months = [ "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug",
	"Sep", "Okt", "Nov", "Dec" ];


var documentPreview;			// active document preview 
var documentPreviewIframe;  	// active iFrame
var isWorkitemLoading=true; 	// indicates if the workitem is still loading
var chornicleSize=1;			// default cronicle size (33%)

/**
 * Init Method for the workitem page
 * 
 *  - set history nav
 * 
 */
$(document).ready(function() {
	
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
	
	// update the link action for each file
	// we redirect the href into the iframe target
	$("[id$='dmslist'] .file-open-link").each(
		function(index, element) {						
			$(this).click(function(){
				var file_link=$(this).attr('href');
				//updateIframe(file_link);
				showDocument($(this).text(),file_link);
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
				// cancel link
			    return false;
			});
		});
		
		
	// set the default preview frame
	documentPreviewIframe=document.getElementById('imixs_document_iframe_embedded');
	
	// autoload first pdf into preview if available.... 
	autoPreviewPDF();
	
	isWorkitemLoading=false;
});


/*
 * This method loads the first pdf and starts a autopreview
 */
function autoPreviewPDF() {
	$("[id$='dmslist'] .file-open-link").each(
		function(index, element) {
			var attachmentName=$(this).text();
			if (attachmentName.endsWith('.pdf') || attachmentName.endsWith('.PDF')) {		
				// if we have a pdf and screen is >1800 than maximize preview.
				if (window.innerWidth>=1800 ) {
					maximizeDocumentPreview();
				}
				$(this).click();
				return false;
			}
		});
}




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
	//$('.imixs-workitem-form').css('width','calc(66.6666% - 0px)');
	$('.imixs-workitem-form .imixs-form').css('width','100%');
	$('.imixs-workitem-form .imixs-document').css('width','0%');
	
	
	$('.imixs-document').hide();
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
	
	if (!isWorkitemLoading) {
		$(".chronicle-tab-documents").click();
	}
}



/*
 * The method shows the document preiview window
 */
function maximizeDocumentPreview() {
	//$('.imixs-workitem-form').css('width','calc(33.333% - 0px)');
	 
	$('.imixs-workitem-form .imixs-form').css('width','50%');
	$('.imixs-workitem-form .imixs-document').css('width','50%');
	$('.imixs-document').show();
	$('.imixs-workitem-document-embedded').hide();
	
	// set preview cookie
	document.cookie = "imixs.office.document.preview=false";
	documentPreview=$('.imixs-document');
	
	// update iframe
	var currentURL="";
	if (documentPreviewIframe) {
		currentURL=documentPreviewIframe.src;
	}
	documentPreviewIframe=document.getElementById('imixs_document_iframe');
	documentPreviewIframe.src=currentURL;
	
	
}


/*
 * The method reduces the with of the chronicle
 */
function expandChronicle() {
	// 33%  calc(33.333% - 20px);
	if (chornicleSize==1) {
		$('.imixs-workitem-form').css('width','58.3333%');
		$('.imixs-workitem-chronicle').css('width','calc(41.6666% - 20px)');
		chornicleSize=2;
		return;
	}
	// 25
	if (chornicleSize==0) {
		$('.imixs-workitem-form').css('width','66.6666%');
		$('.imixs-workitem-chronicle').css('width','calc(33.3333% - 20px)');
		chornicleSize=1;
		return;
	}
}

function shrinkChronicle() {
	if (chornicleSize==1) {
		$('.imixs-workitem-form').css('width','75%');
		$('.imixs-workitem-chronicle').css('width','calc(25% - 20px)');
		chornicleSize=0;
		return;
	}
	if (chornicleSize==2) {
		$('.imixs-workitem-form').css('width','66.6666%');
		$('.imixs-workitem-chronicle').css('width','calc(33.3333% - 20px)');
		chornicleSize=1;
		return;
	}
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
	$('.document-deeplink').attr('href',link);	
	
	// activate preview if minimized!
	if (!isWorkitemLoading && documentPreviewIframe.id==='imixs_document_iframe_embedded') {
		$(".chronicle-tab-documents").click();
	}
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