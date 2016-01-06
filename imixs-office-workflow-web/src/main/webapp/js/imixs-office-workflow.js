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

	/* render typicons */
	layoutTypIcons = function() {
		$('.imixs-typicon').each(
				function() {
					var mainIcon = "", subIcon = "",typClasses, i;

					typClasses = $(this).attr("data-typicon").trim();
					// replace , with ' '
					typClasses = typClasses.replace(/,/g, ' ');

					// only render typicons if data string starts with typicn
					if (typClasses.startsWith('typcn-')) {
						i = typClasses.indexOf('|');
						if (i > 0) {
							mainIcon = typClasses.substring(0, i).trim();
							subIcon = typClasses.substring(i + 1).trim();
						} else {
							mainIcon = typClasses;
						}
						// now render two span tags...
						$(this).empty();
						$(this).append(
								"<span class=\"icon-main typcn " + mainIcon
										+ "\"></span>");
						if (subIcon != "") {
							$(this).append(
									"<span class=\"icon-sub typcn " + subIcon
											+ "\"></span>");
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
		//this.layoutOfficeEditor();
	};

	// public API
	return {
		layoutTypIcons : layoutTypIcons,
		layout : layout
	};

}());

