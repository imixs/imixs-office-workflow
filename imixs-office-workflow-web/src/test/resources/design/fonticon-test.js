
/* icon-main */
$("#icon-main").bind({
	change : function() {
		var iconElement=$( ".icon-main" );
		$(iconElement).removeClass();
		$(iconElement).addClass("icon-main typcn " + $(this).val());
	}
});


/* icon-sub */
$("#icon-sub").bind({
	change : function() {
		
		var iconElement=$( ".icon-sub" );
		$(iconElement).removeClass();
		$(iconElement).addClass("icon-sub icon-sub-ne typcn " + $(this).val());
		
		// Hide if empty
		if (""===$(this).val()) {
			$(iconElement ).hide();
		} else {
			$(iconElement ).show();
		}
	}
});


/* icon-sub position */
$("input[name='icon-sub-pos']").bind({
	click : function() {
		$(".icon-sub").removeClass("icon-sub-ne");
		$(".icon-sub").removeClass("icon-sub-se");
		$(".icon-sub").removeClass("icon-sub-sw");
		$(".icon-sub").removeClass("icon-sub-nw");
		var theme = $(this).val();
		$(".icon-sub").addClass(theme);
	}
});

/* icons theme */
$("input[name='icon-main-theme'], input[name='icon-sub-theme']").bind({
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


	
