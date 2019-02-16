"use strict";

// define namespace
IMIXS.namespace("com.imixs.workflow.office");

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
IMIXS.com.imixs.workflow.office = (function() {
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
	 * initialize the Imixs-Office Application
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
	};
	
	

	// public API
	return {
		layoutAjaxEvent : layoutAjaxEvent,
		initLayout : initLayout
	};

}());



