
/* icon-main */
$("#icon-main").bind({
	change : function() {
		$( ".imixs-viewentry-icon a span" ).removeClass();
		$( ".imixs-viewentry-icon a span" ).addClass("typcn " + $(this).val());
	}
});

/* icon-main theme */
$("input[name^='imixs-viewentry-icon']").bind({
	click : function() {
		
		var elementName=$(this).attr("name");
		elementName="."+elementName.replace("-theme","");
		
		$(elementName).removeClass("imixs-info");
		$(elementName).removeClass("imixs-success");
		$(elementName).removeClass("imixs-warning");
		$(elementName).removeClass("imixs-error");

		var theme = $(this).val();
		$(elementName).addClass(theme);
	}
});


/* icon-ne */
$("#icon-ne").bind({
	change : function() {
	
		
		
		//$(".imixs-viewentry-icon-ne").empty();
		
		$( ".imixs-viewentry-icon-ne span" ).removeClass();
		
		$( ".imixs-viewentry-icon-ne span" ).addClass("typcn " + $(this).val());
		//imixs-viewentry-icon-ne
		
		// Hide if empty
		if (""===$(this).val()) {
			//$( ".imixs-viewentry-icon-ne" ).attr("visibility","hidden");
			$( ".imixs-viewentry-icon-ne" ).hide();
		} else {
			//$( ".imixs-viewentry-icon-ne" ).attr("visibility","visible");
			$( ".imixs-viewentry-icon-ne" ).show();
		}
	}
});
	
