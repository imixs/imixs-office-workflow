// imixs-ml scripts

IMIXS.namespace("com.imixs.workflow.office.ml");

var mlSearchInputID;
/**
 * Init Method for imixs-ml integration. 
 * The method runs only in case a imixsOfficeML.mlResult is defined.
 * The method highlights all ml input items to indicate if the value was a 
 * suggestion from the imixs-ml framework. (see MLAdapter class) 
 *  
 */
$(document).ready(function() {
	
	if (imixsOfficeML.mlResult) {
		fullItemList=$("input[data-item]");
		
		
		// if suggest mode than update the style for input items asociated with ml.entities.
		if (imixsOfficeML.mlResult.status=='suggest') {
			// find all ml data items
			
			$(fullItemList).each(function(){			
				// select data-item value
				var dataItem=$(this).data('item');			
				// test if this data-item is a ml-item
				if (imixsOfficeML.mlResult.items.indexOf(dataItem)>-1) {
					// ok we have a ml item!
					$(this).addClass("imixs-ml");
				}
		    });
		}
		
		// add a keyup event for a search list....
		// this feature is not activated for imixs-date classes!
		$(fullItemList).each(function(){			
			
			// test if imixs-date
			var classNamesDef=$(this).attr("class");
			if (classNamesDef) {
				classNames=classNamesDef.split(/\s+/);
			}
			if (classNames && classNames.indexOf("imixs-date")==-1) {
				// add a keyup handler with delay to serach suggestions....
				$(this).keyup(delay(function (e) {
			      // store the current input id
				  mlSearchInputID=this.name;
				  imixsOfficeML.mlSearch({phrase: this.value });
				}, 500));
				
				
				// hide the suggest list on blur event
				$(this).on("blur",delay(function(event) {
					 $( "[id$=ml-search-results]" ).hide();
				},200));
				
				// turn autocomplete of
				$(this).attr('autocomplete', 'off');
			}
			
	    });
		
	}
	
});


/**
 * This mehtod shows the search result panel and places it below the current input element
 */
function showMLSearchResult(data){
	var status = data.status;
    if (status === "success") {
	
	   // select the inital input element by its name...
	   var inputField = $('input[name ="' + mlSearchInputID + '"]') 
	
		// now we pull the result html list to this input field.....
		$( "[id$=ml-search-results]" ).insertAfter( inputField ).show();
		
	}
}

function selectMLSearchResultEntry(text){
	
   // select the inital input element by its name...
   var inputField = $('input[name ="' + mlSearchInputID + '"]') 
   inputField.val(text);
	
}


/*
 * delay function
 * see: https://stackoverflow.com/questions/1909441/how-to-delay-the-keyup-handler-until-the-user-stops-typing
 */
function delay(callback, ms) {
  var timer = 0;
  return function() {
    var context = this, args = arguments;
    clearTimeout(timer);
    timer = setTimeout(function () {
      callback.apply(context, args);
    }, ms || 0);
  };
}

// Define public namespace
var imixsOfficeML = IMIXS.com.imixs.workflow.office.ml;

