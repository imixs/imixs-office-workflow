/*
 * Updates the 'data-typicon' attribute of the element with class 'imixs-typicon'
 */
function updateTypIconData() {

	console.log("Atacke=" + $("input[name='icon-sub-ne-theme']").val());

	var classes = $("#icon-main").val() + ","
			+ $("input[name='icon-main-theme']:checked").val()

			+ "|" + $("#icon-sub-ne").val() + ","
			+ $("input[name='icon-sub-ne-theme']:checked").val()

			+ "|" + $("#icon-sub-se").val() + ","
			+ $("input[name='icon-sub-se-theme']:checked").val()

			+ "|" + $("#icon-sub-sw").val() + ","
			+ $("input[name='icon-sub-sw-theme']:checked").val()
			
			+ "|" + $("#icon-sub-nw").val()+ ","
			+ $("input[name='icon-sub-nw-theme']:checked").val();
	
	
	$(".imixs-typicon").attr('data-typicon', classes);

	console.log('data-typicon=' + classes);
	imixsOfficeWorkflow.layout();
}

/* icon-main */
$("#icon-main, #icon-sub-ne, #icon-sub-se,#icon-sub-sw,#icon-sub-nw").bind({
	change : function() {
		// var iconElement=$( ".icon-main" );
		// $(iconElement).removeClass();
		// $(iconElement).addClass("icon-main typcn " + $(this).val());
		updateTypIconData();
	}
});

$("input[name='icon-main-theme'], input[name='icon-sub-ne-theme'], input[name='icon-sub-se-theme'], input[name='icon-sub-sw-theme'], input[name='icon-sub-nw-theme']")
		.bind({
			click : function() {
				updateTypIconData();
			}
		});
