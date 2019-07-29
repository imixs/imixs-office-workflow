// workitem scripts

IMIXS.namespace("com.imixs.workflow.office.workitem");

var months = [ "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug",
	"Sep", "Okt", "Nov", "Dec" ];



/**
 * Init Method for the workitem page
 * 
 *  - set history nav
 * 
 * @returns
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
	imixsOfficeWorkflow.imixs_chronicle=c_value.indexOf("imixs.office.chronicle=true")>-1;
	
	imixsOfficeWorkflow.imixs_chronicle_comments=true;
	imixsOfficeWorkflow.imixs_chronicle_nav=JSON.parse('{ "comment" : true, "files":true, "version":true, "reference":true }'); 
		
	if (imixsOfficeWorkflow.imixs_chronicle) {
		// avoid slide effect on first load....
		$('.imixs-workitem-chronicle').css('transition','0.0s');
		$('.imixs-workitem-container').css('transition','0.0s');
	
		showChronicle();
		$('.imixs-workitem-chronicle').css('transition','0.3s');
		$('.imixs-workitem-container').css('transition','0.3s');
	} else {
		
	}
	$('.imixs-workitem-chronicle').show();
});



/* Open the chronical nav on the right side */
function toggleChronicle() {
	if (!imixsOfficeWorkflow.imixs_chronicle) {
		showChronicle();
	} else {
		hideChronicle();
	}
	imixsOfficeWorkflow.imixs_chronicle=!imixsOfficeWorkflow.imixs_chronicle;
	// set chronicle cookie
	document.cookie = "imixs.office.chronicle="+imixsOfficeWorkflow.imixs_chronicle;
}
function showChronicle() {
	// open chronicle 
	$('.imixs-workitem-chronicle').css('width','33.3333%');
	$('.imixs-workitem-container').css('width','66.6666%');
	
	$('.imixs-workitem-chronicle .nav').hide();
	$('.imixs-workitem-chronicle .content').show();
}
function hideChronicle() {
	// close chronicle
	$('.imixs-workitem-chronicle').css('width','60px');
	$('.imixs-workitem-container').css('width','calc(100% - 60px)');
	
	$('.imixs-workitem-chronicle .content').hide();
	$('.imixs-workitem-chronicle .nav').show();
}







//change toggle state of a header panel
function togglePanel(buttonid, panelid) {
	$(panelid).toggle();
	if ($(panelid).is(":hidden")) {
		// do this
		$(buttonid).button({
			icons : {
				secondary : "ui-icon-plus"
			}
		}).removeClass('active');
	} else {
		$(buttonid).button({
			icons : {
				secondary : "ui-icon-minus"
			}
		}).addClass('active');
	}
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