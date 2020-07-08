// workitem scripts

IMIXS.namespace("com.imixs.workflow.office.workitem");

var months = [ "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug",
	"Sep", "Okt", "Nov", "Dec" ];



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
				updateIframe(file_link);
					
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
				updateIframe(file_link);
				showDocument($(this).text(),file_link);
				
				// click into document tab if resolution <1800
				if (window.innerWidth<1800) {
					$(".chronicle-tab-documents").click();
				}
				// cancel link
			    return false;
			});
		});
		
		
	/* autoload first pdf into preview if available.... */
	autoPreviewPDF();
	
});


/*
 * This method loads the first pdf and starts a autopreview
 */
function autoPreviewPDF() {
	$("[id$='dmslist'] .file-open-link").each(
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
 * Hide the document preiview window
 */
function hideDocument() {
	// can be disabled by the property 'feature.document.preview=false'
	//if (windowWidth>=1800 && imixsOfficeWorkflow.document_preview) {
		$('.imixs-workitem-form').css('width','calc(66.6666% - 0px)');
		$('.imixs-workitem-document .document-title').text('');
		$('.imixs-workitem-document').hide();
		// set chronicle cookie
		document.cookie = "imixs.office.document=false";
	//}
}

/*
 * A document preview window will be shown if the widow with is >= 1800 
 * and the feature switch is not disabled
 */
function showDocument(title, file_link) {
	
	var windowWidth = window.innerWidth;
	// can be disabled by the property 'feature.document.preview=false'
	if (windowWidth>=1800 && imixsOfficeWorkflow.document_preview) {
		$('.imixs-workitem-form').css('width','calc(33.333% - 0px)');
		$('.imixs-workitem-document').show();
		// cut title if length >32 chars
		if (title.length>32) {
			title=title.substring(0,32)+"...";
		}
		$('.imixs-workitem-document .document-title').text(title);
		// set chronicle cookie
		document.cookie = "imixs.office.document=true";
		
		// disable embedded iframe
		iframe = document.getElementById('imixs_document_iframe_embedded');
		//$(iframe).contents().find("body").html("");//.src = null;
		iframe.src="";
		$("#documents-file-deeplink-embedded").hide();
		
		// update documents-file-deeplink
		$("#documents-file-deeplink").attr('href',file_link);
		$("#documents-file-deeplink").show();
			
			
	} else {
		// update documents-file-deeplink
		$("#documents-file-deeplink-embedded").show();
		$("#documents-file-deeplink-embedded").attr('href',file_link);
			
	}
}

/*
 * This method changes the target of a attachment link to the iframe
 */
function updateIframe(docurl) {
	//console.log(docurl);
	$("#document_preview_helper").hide();
	
	var iframe;
	var windowWidth = window.innerWidth;
	// depending on the window with the the feature switch 
	// two different ifraemes will be used to display the document preview
	if (windowWidth>=1800 && imixsOfficeWorkflow.document_preview) {
		iframe = document.getElementById('imixs_document_iframe');
		$("#documents-file-deeplink-embedded").hide();
	} else {
		
		iframe = document.getElementById('imixs_document_iframe_embedded');
		$("#documents-file-deeplink-embedded").show();
	}
	
	iframe.src = docurl;
}


/**
 * Wokritem References - print year/month sections
 * 
 * @returns
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