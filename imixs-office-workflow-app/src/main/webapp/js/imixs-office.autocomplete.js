// Imixs-Office Autocomplete Script
//
// Used for a generic autocomplete feature for input fields

IMIXS.namespace("org.imixs.workflow.office.autocomplete");

var autocompleteInputID;
var autcompleteSelectedElement;
var autocompleteSearchReady=false;



/* Helper method to highligt the current element in the result list */
function autocompleteSelectNextElement() {
	var resultElementList = $(".autocomplete-resultlist-element");
	// remove acitve if set	
	$(resultElementList).removeClass("active");
	// next element...?
	autcompleteSelectedElement++;
	if (autcompleteSelectedElement >= resultElementList.length) {
		// reset if overflow...
		autcompleteSelectedElement = 0;
	}
	// set new active
	$(resultElementList[autcompleteSelectedElement]).addClass("active");
}

function autocompleteSelectPrevElement() {
	var resultElementList = $(".autocomplete-resultlist-element");
	// remove acitve if set	
	$(resultElementList).removeClass("active");
	autcompleteSelectedElement--;
	// next element...?
	if (autcompleteSelectedElement < 0) {
		autcompleteSelectedElement = resultElementList.length - 1;
	}
	// set new active
	$(resultElementList[autcompleteSelectedElement]).addClass("active");
}


/**
 * This mehtod shows the search result panel and places it below the current input element
 */
function autocompleteShowResult(data) {
	autcompleteSelectedElement = -1;
	var status = data.status;
	if (status === "success") {
		// select the inital input element by its name...
		var inputField = $('input[name ="' + autocompleteInputID + '"]')

		// now we pull the result html list to this input field.....
		$("[id$=autocomplete-resultlist]").insertAfter(inputField).show();
	}
}

function autocompleteSelectElement(text) {
	// select the inital input element by its name...
	var inputField = $('input[name ="' + autocompleteInputID + '"]')
	inputField.val(text);
}


/*
 * initializes an input element for autocompletion. 
 */
function autocompleteInitInput(inputElement,searchCallback) {
	// add a input event handler with delay to serach for suggestions....
	$(inputElement).on('input', delay(function() {
		if (!autocompleteSearchReady) {
			return; // start only after first key down! (see below)
		}
		// store the current input id
		autocompleteInputID = inputElement.name;
		//imixsOfficeML.mlSearch({ phrase: this.value });
		searchCallback({ phrase: this.value });
	}, 500)).trigger('input');


	// hide the suggest list on blur event
	$(inputElement).on("blur", delay(function(event) {
		$("[id$=autocomplete-resultlist]").hide();
	}, 200));


	/*execute a function presses a key on the keyboard:*/
	$(inputElement).keydown(function(e) {
		autocompleteSearchReady=true; // init serach mode
		if (e.keyCode == 40) {
	        /*If the arrow DOWN key is pressed,
	        increase the currentFocus variable:*/
			autocompleteSelectNextElement();
		} else if (e.keyCode == 38) { //up
	        /*If the arrow UP key is pressed,
	        decrease the currentFocus variable:*/
			autocompleteSelectPrevElement();
		} else if (e.keyCode == 13) {
			/*If the ENTER key is pressed, prevent the form from being submitted,*/
			e.preventDefault();
			var resultElementListActive = $(".autocomplete-resultlist-element.active");
			//if (mlResultFocus >= 0 && mlResultFocus < resultElementList.length) {
			$("a",resultElementListActive).click();
				//$("a", resultElementList[mlResultFocus]).click();
				// hide list
			$("[id$=autocomplete-resultlist]").hide();
			//}
		}
	});

	// turn autocomplete of
	$(inputElement).attr('autocomplete', 'off');
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
		timer = setTimeout(function() {
			callback.apply(context, args);
		}, ms || 0);
	};
}

// Define public namespace
var imixsOfficeAutocomplete = IMIXS.org.imixs.workflow.office.autocomplete;

