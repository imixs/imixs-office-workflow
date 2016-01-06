"use strict";

// define namespace
IMIXS.namespace("com.imixs.workflow.office");

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

	/*
	 * render typicons
	 * 
	 * expected format= classnames: main|sub-ne|sub-ns|...
	 */
	layoutTypIcons = function() {
		$('.imixs-typicon')
				.each(
						function() {
							var mainIcon = "", subIconNe = "", subIconSe = "", subIconSw = "", subIconNw = "", typClasses, i, typiconElement;

							typClasses = $(this).attr("data-typicon").trim();
							// replace , with ' '
							typClasses = typClasses.replace(/,/g, ' ');

							// only render typicons if data string starts with
							// typicn
							if (typClasses.startsWith('typcn-')) {

								var typiconElements = typClasses.split('|');
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
													+ mainIcon + "\"></span>");
								}

								if (subIconNe && subIconNe.startsWith('typcn-')) {
									$(this).append(
											"<span class=\"icon-sub icon-sub-ne typcn "
													+ subIconNe + "\"></span>");
								}
								
								
								if (subIconSe && subIconSe.startsWith('typcn-')) {
									$(this).append(
											"<span class=\"icon-sub icon-sub-se typcn "
													+ subIconSe + "\"></span>");
								}
								
								if (subIconSw && subIconSw.startsWith('typcn-')) {
									$(this).append(
											"<span class=\"icon-sub icon-sub-sw typcn "
													+ subIconSw + "\"></span>");
								}
								
								if (subIconNw && subIconNw.startsWith('typcn-')) {
									$(this).append(
											"<span class=\"icon-sub icon-sub-nw typcn "
													+ subIconNw + "\"></span>");
								}
							}
						});
	},

	/**
	 * initialize the Imixs-Office Application
	 */
	layout = function(settings) {
		// Force options to be an object
		settings = settings || {};

		// waiting feature
		var $body = $("body");
		// ajax
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

		// layout
		this.layoutTypIcons();
		// this.layoutOfficeEditor();
	};

	// public API
	return {
		layoutTypIcons : layoutTypIcons,
		layout : layout
	};

}());
