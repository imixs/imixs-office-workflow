$("input[name='icontheme']").bind({
	click : function() {

		$(".imixs-viewentry-icon").removeClass("imixs-info");
		$(".imixs-viewentry-icon").removeClass("imixs-success");
		$(".imixs-viewentry-icon").removeClass("imixs-warning");
		$(".imixs-viewentry-icon").removeClass("imixs-error");

		var theme = $(this).val();
		$(".imixs-viewentry-icon").addClass(theme);
	}
});



$("#icontheme-ne").bind({
	change : function() {
	
		
		
		//$(".imixs-viewentry-icon-ne").empty();
		
		$( ".imixs-viewentry-icon-ne span" ).removeClass();
		
		$( ".imixs-viewentry-icon-ne span" ).addClass("typcn " + $(this).val());
		//imixs-viewentry-icon-ne
	}
});
	
