"use strict";

// define namespace
IMIXS.namespace("org.imixs.workflow.office");


/**
 * This method initializes the main layout and general office functionality.
 * 
 *  - general Office Layout
 *  - init tinyMCE
 *  - set calender week start
 *  - search nav input
 */
$(document).ready(function() {
	
	// init layout
	imixsOfficeMain.initLayout();
	imixsOfficeMain.layoutOfficeEditor();
	
	// customize datePicker....show calendar week
	$('.imixs-date').datepicker('option', 'showWeek', imixsOfficeMain.imixs_date_showWeek);
	
	// hit enter on nav search box trigers search link..
	$("[data-id='nav_input_phrase']").keydown(function(e){
	    if(e.keyCode == 13) {
	    	$("[data-id='nav_search_link']").click();
	    	return false;
	    }
	});

	// check if we have a dashboard with an open admin panel
	const dashboardPanel = document.querySelector('.dashboard-admin-panel');
	if (dashboardPanel) {
		const cookieValue = imixsOfficeMain.getCookie('imixs.office.dashboard.admin');
		// Remove 'open' class first
		dashboardPanel.classList.remove('open');
		// Add 'open' class only if cookie is 'true'
		if (cookieValue === 'true') {
			dashboardPanel.classList.add('open');
		}
		// Enable transitions after initial setup
		setTimeout(() => {
			dashboardPanel.style.transition = 'width 0.2s ease';
		}, 100);
    }
});



/*
 * Method to render typicons
 * 
 * expected format= classnames: main|sub-ne|sub-ns|...
 */
$.fn.layoutTypIcons = function(options) {
	return this
			.each(function() {

				$('.imixs-typicon', this)
						.each(
								function() {
									
									var mainIcon = "", subIconNe = "", subIconSe = "", subIconSw = "", subIconNw = "", typClasses, i, typiconElement;

									typClasses = $(this).attr("data-typicon");
									if (typClasses) {
										typClasses = $(this).attr(
												"data-typicon").trim();

										// replace , with ' '
										typClasses = typClasses.replace(/,/g,
												' ');
									} else {
										typClasses = "";
									}

									// only render typicons if data string
									// starts with typicn
									if (typClasses.indexOf('typcn-') === 0) {

										var typiconElements = typClasses
												.split('|');
										mainIcon = typiconElements[0];
										subIconNe = typiconElements[1];
										subIconSe = typiconElements[2];
										subIconSw = typiconElements[3];
										subIconNw = typiconElements[4];

										// now render the span tags...
										$(this).empty();
										if (mainIcon != "") {
											$(this).append(
													"<span class=\"icon-main typcn "
															+ mainIcon
															+ "\"></span>");
										}

										if (subIconNe
												&& subIconNe.indexOf('typcn-') === 0) {
											
											$(this).append(
													"<span class=\"icon-sub icon-sub-ne typcn "
															+ subIconNe
															+ "\"></span>");
										}

										if (subIconSe
												&& subIconSe.indexOf('typcn-') === 0) {
											$(this).append(
													"<span class=\"icon-sub icon-sub-se typcn "
															+ subIconSe
															+ "\"></span>");
										}

										if (subIconSw
												&& subIconSw.indexOf('typcn-') === 0) {
											$(this).append(
													"<span class=\"icon-sub icon-sub-sw typcn "
															+ subIconSw
															+ "\"></span>");
										}

										if (subIconNw
												&& subIconNw.indexOf('typcn-') === 0) {
											$(this).append(
													"<span class=\"icon-sub icon-sub-nw typcn "
															+ subIconNw
															+ "\"></span>");
										}
									}
								});

			});
};

// define core module
IMIXS.org.imixs.workflow.office = (function() {
	if (!IMIXS.org.imixs.core) {
		console.error("ERROR - missing dependency: imixs-core.js");
	}

	var imixs = IMIXS.org.imixs.core,

	/***************************************************************************
	 * 
	 * module methods
	 * 
	 **************************************************************************/

	/**
	 * Global Layout method 
	 * 
	 * - Initialize ajax loading feature 
	 * - Layout Typ Icons 
	 */
	initLayout = function(settings) {
		
		// Force options to be an object
		settings = settings || {};

		// add waiting feature
		var $body = $("body");
		$body.removeClass("loading");

		// add ajax support
		$(document).on({
			ajaxStart : function() {
				$body.addClass("loading");
			},
			ajaxStop : function() {
				$body.removeClass("loading");
			}
		});
		// form submit
		$('form').on('submit', function(e) {
			$body.addClass("loading");
		});
		
		$(document).layoutTypIcons();
	},
	
	

	/**
	 * Tiny MCE Editor
	 * 
	 *  - define imixs-editor
	 *  - define imixs-editor-basic
	 * 
	 */
	/* laest sich im moment nicht auslagern (script_url!) see issue #87 */
	layoutOfficeEditor = function() {
		// Layout office default editor
		tinymce.init({
			  selector: 'textarea.imixs-editor',
			  height: 300,
			  plugins: [
			    'advlist autolink lists link image preview',
			    'searchreplace code fullscreen',
			    'contextmenu paste code'
			  ],
				paste_data_images: true,
				paste_preprocess : function(pl, o) {
				    if (o.content.length>160000) {
				 		alert(imixsOfficeMain.error_message_mce_image_size);
				 		o.content="";
				    }
				},
			  toolbar: 'insertfile undo redo | styleselect | bold italic | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link image | removeformat',
			  content_css : imixsOfficeMain.contextPath+"/layout/css/tinymce.css"
			});
		
		// layout tinymce for basic
		tinymce
			.init({
				selector : 'textarea.imixs-editor-basic',
				height : 200,
				menubar : false,
				statusbar : true,
				plugins : [ 'advlist autolink lists link image',
						'searchreplace code fullscreen', 'contextmenu paste code' ],
				paste_data_images: true,
				paste_preprocess : function(pl, o) {
				    if (o.content.length>160000) {
				    	alert(imixsOfficeMain.error_message_mce_image_size);
				 		o.content="";
				    }
				},
				toolbar : 'undo redo | styleselect | bold italic | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link image | removeformat code',
				content_css : imixsOfficeMain.contextPath+"/layout/css/tinymce.css"

			});
	},

	/**
	 * This method layouts a page or page fragment triggered by an ajax event.
	 * The method shows a waiting panel and refresh the page layout after the
	 * ajax event is completed. Optional a jquery context object can be passed
	 * to this method to force an parcial update of a page.
	 * The context can also be a String selector. 
	 * 
	 */
	layoutAjaxEvent = function(data, context) {
		// if context is string replace : with \:
		if (typeof context === 'string' || context instanceof String) {
			context='#'+context;
			context = context.replace(/:/g, "\\:");
		}
		if (data.status === 'begin') {
			if (context) {
				// waiting feature
				$(context).addClass("loading");
			} else {
				$("body").addClass("loading");
			}
		}
		if (data.status === 'success') {
			// refresh office layout....
			if (context) {
				$(context).removeClass("loading");
				$(context).imixsLayout();
				$(context).layoutTypIcons();
				$("span.imixs-tooltip",context).layoutImixsTooltip();
			} else {
				$("body").removeClass("loading");
				$(document).imixsLayout();
				$(document).layoutTypIcons();				
				$("span.imixs-tooltip",document).layoutImixsTooltip();
			}
		}
	},
	
	setCookie = function(cname, cvalue, exdays=999) {
	  var d = new Date();
	  d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
	  var expires = "expires="+d.toUTCString();
	  document.cookie = cname + "=" + cvalue + ";" + expires + ";SameSite=strict;path=/";
	},

	getCookie = function(name) {
		const value = `; ${document.cookie}`;
		const parts = value.split(`; ${name}=`);
		if (parts.length === 2) return parts.pop().split(';').shift();
		return null;
    },

	toggleDashboardPanel = function () {
		const panel = document.querySelector('.dashboard-admin-panel');
		panel.classList.toggle('open');
		// Set cookie based on whether panel has 'open' class
		const isOpen = panel.classList.contains('open');
		setCookie('imixs.office.dashboard.admin', isOpen, 14);
	},
	
	// redirect to a given workitem uid
	openWorkitemByID = function(uid) {
		document.location.href=imixsOfficeMain.contextPath+"/pages/workitems/workitem.xhtml?id="+uid;
	};

	// public API
	return {
		layoutAjaxEvent : layoutAjaxEvent,
		initLayout : initLayout,
		openWorkitemByID : openWorkitemByID,
		layoutOfficeEditor : layoutOfficeEditor,
		setCookie : setCookie,
		getCookie : getCookie,
		toggleDashboardPanel: toggleDashboardPanel
	};

}());

// Define public namespace
var imixsOfficeMain = IMIXS.org.imixs.workflow.office;

