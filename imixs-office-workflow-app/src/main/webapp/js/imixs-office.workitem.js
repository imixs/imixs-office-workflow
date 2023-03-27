"use strict";

// workitem scripts

IMIXS.namespace("org.imixs.workflow.workitem");

var months = [ "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug",
	"Sep", "Okt", "Nov", "Dec" ];


var documentPreview;			// active document preview element
var documentPreviewIframe;  	// active iFrame
var documentPreviewURL;			// URL for current displayed document
var isWorkitemLoading=true; 	// indicates if the workitem is still loading
var chornicleSize=1;			// default cronicle size (33%)
var chroniclePreview=true; 		// indicates if documetns should be shown in the cornicle column
var callbackRegistrySaveWorkitem=[];
var isChronicleResizing=false 		// tangable divider
var sliderPosX;				    // current position of moving slider
var workitemElement;
var workitemFormElement;			// the chronical bar
var workitemSliderElement;
var workitemChronicleElement;

/**
 * Init Method for the workitem page
 * 
 *  - set history nav
 * 
 */
$(document).ready(function() {
	
	imixsOfficeMain.imixs_document=true; // default
	imixsOfficeMain.imixs_chronicle_comments=true;
	imixsOfficeMain.imixs_chronicle_nav=JSON.parse('{ "comment" : true, "files":true, "version":true, "reference":true }'); 
	
	// read croncicle document preview form cookie
	chroniclePreview=imixsOfficeWorkitem.readCookie('imixs.office.document.chronicle.preview');
	if (chroniclePreview == "true") {
		chroniclePreview=true;
	} else {
		chroniclePreview=false;
	}

	// Init tangible slider
	workitemElement = document.querySelector('.imixs-workitem');
	workitemFormElement=document.querySelector('.imixs-workitem > .imixs-workitem-form');
	workitemChronicleElement=document.querySelector('.imixs-workitem > .imixs-workitem-chronicle');
	workitemSliderElement = document.querySelector('.imixs-workitem > .imixs-slider');
	let lastFormWidth=imixsOfficeWorkitem.readCookie('imixs.office.document.workitem.formwidth');
	// set a default if cookie not yet defined
	if (!lastFormWidth) {
		lastFormWidth=workitemElement.offsetWidth*0.75;
	} 
	lastFormWidth=imixsOfficeWorkitem.validateFormMinMaxWidth(lastFormWidth);
    imixsOfficeWorkitem.updateFormWidth(lastFormWidth);
	workitemSliderElement = document.querySelector('.imixs-workitem > .imixs-slider');
	workitemSliderElement.addEventListener('mousedown', (e) => {
	  isChronicleResizing = true;
	  sliderPosX = e.clientX;
	});
	window.addEventListener('mouseup', (e) => {
	  isChronicleResizing = false;
	});
	window.addEventListener('mousemove', (e) => {
	  if (!isChronicleResizing) return;
	  let _newWidth=workitemFormElement.offsetWidth + (e.clientX - sliderPosX);
	  // adjust minwidth....
	  _newWidth=imixsOfficeWorkitem.validateFormMinMaxWidth(_newWidth);
	  imixsOfficeWorkitem.updateFormWidth(_newWidth);
	  sliderPosX = e.clientX;
	  
	});

	$('.document-nav').hide();
	
	imixsOfficeWorkitem.updateAttachmentLinks();
		
	// set the default preview frame
	documentPreviewIframe=document.getElementById('imixs_document_iframe_embedded');
	
	// autoload first pdf into preview if available.... 
	imixsOfficeWorkitem.autoPreviewPDF();

	imixsOfficeWorkitem.initSignaturePad();
	
	isWorkitemLoading=false;
});

/*
 * This callback method is triggered by the imxs-faces.js file upload
 * component. The method updates the deep links for uploaded files
 * and loads pdf files into the preview window
 */
function onFileUploadChange() {
	
	$('.document-nav').hide();
	// cancel current preview....
	documentPreviewIframe.src="";
	//contentWindow.location.replace("");
	documentPreviewURL="";
	
	// update deep links
	imixsOfficeWorkitem.updateAttachmentLinks();
		
	// auto preview
	$(".imixsFileUpload_uploadlist_name a").each(
		function(index, element) {						
			var attachmentName=$(this).text();
			if (attachmentName.endsWith('.pdf') || attachmentName.endsWith('.PDF')) {		
				// if we have a pdf and screen is >1800 than maximize preview.
				if (window.innerWidth>=1800 ) {
					imixsOfficeWorkitem.maximizeDocumentPreview();
				}
				var link=$(this).attr('href');
				// encode link...
				var encodedAttachmentName=encodeURIComponent(attachmentName);
				link=link.replace(attachmentName,encodedAttachmentName);
				imixsOfficeWorkitem.showDocument($(this).text(),link);
				return false;
			}
		});
}


// define core module
IMIXS.org.imixs.workflow.workitem = (function() {
	if (!IMIXS.org.imixs.core) {
		console.error("ERROR - missing dependency: imixs-core.js");
	}

	var imixs = IMIXS.org.imixs.core,
	
	readCookie = function(name) {
	    var nameEQ = name + "=";
	    var ca = document.cookie.split(';');
	    for (var i = 0; i < ca.length; i++) {
	        var c = ca[i];
	        while (c.charAt(0) == ' ') c = c.substring(1, c.length);
	        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
	    }
	    return null;
	},
	
	setCookie = function(cname, cvalue, exdays=999) {
	  var d = new Date();
	  d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
	  var expires = "expires="+d.toUTCString();
	  document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/";
	},
	
	
	/*
	 * This method loads the first pdf and starts a autopreview
	 * if chroniclePreview == true
	 */
	autoPreviewPDF = function() {
		//if (!chroniclePreview) return;
		$("[id$='dmslist'] .file-open-link").each(
			function(index, element) {
				var attachmentName=$(this).text();
				if (attachmentName.endsWith('.pdf') || attachmentName.endsWith('.PDF')) {		
					// if we have a pdf and screen is >1800 than maximize preview.
					if (window.innerWidth>=1800 || chroniclePreview==false ) {
						maximizeDocumentPreview();
					}
					//$(this).click();
					showDocument($(this).text(),$(this).attr('href'));
					return false;
				}
			});			
	},

	/*
	 * This method initializes a signature pad if part of the form.
	 * The signature widget supports a 2d canvas to draw a signature
	 * with the mouse cursor. 
	 */
	initSignaturePad = function() {
		const signatureCanvas = document.querySelector('.imixs-signature-pad canvas');
		const workitemForm = document.querySelector('#workitem_form');
		const signatureClearButton = document.querySelector('.imixs-signature-clear-action');
		const ctx = signatureCanvas.getContext('2d');
		ctx.lineWidth = 3; 
		ctx.lineJoin = ctx.lineCap = 'round';
		let writingMode = false;

		// add event listeners
		signatureCanvas.addEventListener('pointerdown', (event) => { 
			writingMode = true; 
			ctx.beginPath(); 
			const positionX = event.clientX - event.target.getBoundingClientRect().x; 
			const positionY = event.clientY - event.target.getBoundingClientRect().y; 			
			ctx.moveTo(positionX, positionY); 			
			}, { passive: true }
		); 
		
		signatureCanvas.addEventListener('pointerup',  (event) => { 
			writingMode = false;
		}, { passive: true }); 

		signatureCanvas.addEventListener('pointermove', (event) => { 
			if (!writingMode) {
				return;
			}
			const positionX = event.clientX - event.target.getBoundingClientRect().x; 
			const positionY = event.clientY - event.target.getBoundingClientRect().y; 
			ctx.lineTo(positionX, positionY); 
			ctx.stroke(); 			
		}, { passive: true });

		// clear button
		signatureClearButton.addEventListener('click', (event) => { 
			event.preventDefault(); 
			ctx.clearRect(0, 0, signatureCanvas.width, signatureCanvas.height);
		});

		// submit event
		workitemForm.addEventListener('submit', (event) => {
			// crate image url...
			const imageURL = signatureCanvas.toDataURL(); 
			// find hidden field to store the url data...
			let nextElement = signatureCanvas.nextElementSibling;
			while (nextElement) {
				if (nextElement.tagName === 'INPUT' && nextElement.type === 'hidden') {
				  	nextElement.value=imageURL;
				  	break;
				}
				nextElement = nextElement.nextElementSibling;
			}
		})
	},

	/*
	 * This helper method updates the attachment links
     * in the chronicle main view and in the dms list.
     * Attachments are opend in the preview window 
     */ 
	updateAttachmentLinks = function() {
		// update the link action for each file
		// we redirect the href into the iframe target
		$("[id$='dmslist'] .file-open-link").each(
			function(index, element) {						
				$(this).click(function(){
					var file_link=$(this).attr('href');
					//updateIframe(file_link);
					showDocument($(this).text(),file_link);
					// cancel link
				    return false;
				});
			});
			
			
		// we redirect also the href from chronicle into the iframe target
		
		// chronicle-main  attachmentlink
		$("[id$='chronicle-main'] .attachmentlink").each(
		//$(".files a.attachmentlink").each(
			function(index, element) {						
				$(this).click(function(){
					var file_link=$(this).attr('href');
					//updateIframe(file_link);
					showDocument($(this).text(),file_link);
					// cancel link
				    return false;
				});
			});
				
				
		// chronicle-main  imixsFileUpload link
		$(".imixsFileUpload_uploadlist_name a").each(
			function(index, element) {						
				$(this).click(function(){
					// we need to encode the filename within the link
					var attachmentName=$(this).text();
					var link=$(this).attr('href');
					// encode link...
					var encodedAttachmentName=encodeURIComponent(attachmentName);
					link=link.replace(attachmentName,encodedAttachmentName);
					showDocument($(this).text(),link);
					// cancel link
				    return false;
				});
			});
	},
	
	
	

	/*
	 * This method clears the document preview
     * the method is called after a remove click
     */
	clearDocumentPreview = function(event) {
		 if (event.status === 'success') {
			documentPreviewIframe.contentWindow.location.replace("");
			documentPreviewIframe.src="";
			documentPreviewURL="";
			updateAttachmentLinks();
			// autoload first pdf into preview if available.... 
			autoPreviewPDF();	
		}
	},

    /*
     * This helper method adjusts the minimum/maximum 
     * width of the from view
     */
	validateFormMinMaxWidth = function (_width) {	
		// adjust minwidth....
	    if (_width<500) {
	  	  _width=500;
	  	  isChronicleResizing = false;
	    }
	    if (_width>workitemElement.offsetWidth-340) {
	  	  _width=workitemElement.offsetWidth-340;
	  	  isChronicleResizing = false;
	    }
	    return _width;
	},

	/*
	 * This method hides the document preiview window
	 * and shows the history tab.
	 */
	closeDocumentPreview = function () {	
		minimizeDocumentPreview();
		// switch to chronicle
		$(".chronicle-tab-history").click();
	},

	/*
	 * The method hides the document preview window and opens 
	 * the document in the minimized preview on the documents tab
	 */
	minimizeDocumentPreview =function () {
		$('.imixs-workitem-form .imixs-form').removeClass('split');
		$('.imixs-workitem-form .imixs-document').removeClass('split');
		$('.imixs-workitem-form .imixs-document').hide();
		$('.imixs-workitem-document-embedded').show();
	
		// set preview cookie
		imixsOfficeWorkitem.setCookie("imixs.office.document.chronicle.preview","true",14);
		documentPreview=$('.imixs-workitem-document-embedded');
		// update iframe
		documentPreviewIframe=document.getElementById('imixs_document_iframe_embedded');
		documentPreviewIframe.contentWindow.location.replace(documentPreviewURL);
		
		if (!isWorkitemLoading) {
			$(".chronicle-tab-documents").click();
		}
	},


	
	/*
	 * The method shows the document preiview window
	 */
	maximizeDocumentPreview = function () {
		$('.imixs-workitem-form .imixs-form').addClass('split');
		$('.imixs-workitem-form .imixs-document').addClass('split');
		$('.imixs-document').show();
		$('.imixs-workitem-document-embedded').hide();
		
		// set preview cookie
		//document.cookie = "imixs.office.document.chronicle.preview=false; path=/";
		imixsOfficeWorkitem.setCookie("imixs.office.document.chronicle.preview","false",14);
		documentPreview=$('.imixs-document');
		// update iframe
		documentPreviewIframe=document.getElementById('imixs_document_iframe');
		documentPreviewIframe.contentWindow.location.replace(documentPreviewURL);
		
	},

	
	/*
	 * updates the workitem form width and update the corresponding cookie
	 */	
	updateFormWidth=function(_newWidth) {	
      const chronicleWidth=workitemElement.offsetWidth - _newWidth - workitemSliderElement.offsetWidth;
      workitemChronicleElement.style.flexBasis = `${chronicleWidth}px`;
      imixsOfficeWorkitem.setCookie("imixs.office.document.workitem.formwidth",_newWidth,14);
	},
		
	/*
	 * reduce the with of the chronicle
	 */
	expandChronicle= function () {
		let newSize=validateFormMinMaxWidth(workitemElement.offsetWidth/2);
		imixsOfficeWorkitem.updateFormWidth(newSize);
	},

	/*
	 * increase the with of the chronicle
	 */
	shrinkChronicle = function () {
		let newSize=validateFormMinMaxWidth(workitemElement.offsetWidth-340);
		imixsOfficeWorkitem.updateFormWidth(newSize);
	},

	/*
	 * A document loads the current document (link) into the documentPreviewIframe
	 * and displays the document title.
	 
	 */
	showDocument=function (title, link) {	
		if (!link || link=="") {
			return; // no url defined!
		}	
		// cut title if length >64 chars
		if (title.length>64) {
			title=title.substring(0,64)+"...";
		}
		$('.document-title',documentPreview).text(title);
		documentPreviewURL=link;
		documentPreviewIframe.contentWindow.location.replace(documentPreviewURL);
		
		// update deeplink
		$('.document-deeplink').attr('href',documentPreviewURL);	
		
		// activate preview if minimized!
		if (!isWorkitemLoading && documentPreviewIframe.id==='imixs_document_iframe_embedded') {
			toggleChronicleDocuments();
		}		
		$('.document-nav').show();
	},

	
	/*
	 * This method toggles into the chronicle documents view
	 */
	toggleChronicleHistory=function () {
		$('.chronicle-tab-history').parent().addClass('active');
		$('.chronicle-tab-documents').parent().removeClass('active');
		$('#imixs-workitem-chronicle-tab-documents').hide();
		$('#imixs-workitem-chronicle-tab-history').show();
		// set a right margin for history view only
		$('.imixs-workitem-chronicle-content').css('width','calc(100% - 30px)');
	
	},
	toggleChronicleDocuments=function () {
		$('.chronicle-tab-documents').parent().addClass('active');
		$('.chronicle-tab-history').parent().removeClass('active');
		$('#imixs-workitem-chronicle-tab-history').hide();
		$('#imixs-workitem-chronicle-tab-documents').show();
		// set a right margin for history view only
		$('.imixs-workitem-chronicle-content').css('width','calc(100% - 0px)');
	},


	registerSaveWorkitemListener=function (callback) {
		callbackRegistrySaveWorkitem.push(callback);
	},

    /*
     * Helper method handles registered callback methods
     */
	saveWorkitemHandler=function (confirmMessage,uiWorkflowAction) {
		if (confirmMessage) {
			if (confirm(confirmMessage)==false) {
				return false;
			}
		}
		// do we have callbacks?
		if (callbackRegistrySaveWorkitem) {
			for (const saveCallback of callbackRegistrySaveWorkitem) {
			  var callBackResult=saveCallback(uiWorkflowAction);
	 		  if (callBackResult===false) {
		         	return false;
			  } 
		    }    
		}	
		return true;
	},


	/**
	 * WorkitemRef Input Widget
	 */

	/*
	 * initializes an input element for autocompletion. 
	 * the param 'resultlistid' is optional and defines the element 
	 * containing the search result.
	 * The callback method is optional and triggered when a new element was selected 
	 * from the suggest list
	 */
	workitemRefInitInput = function (inputElement,searchCallback, resultlistId, selectCallback) {
	
		// set id for result list element
		if (!resultlistId || resultlistId==='') {
			resultlistId='autocomplete-resultlist'; // default name
		} 
		$(inputElement).attr('data-resultlist', resultlistId);
		
		// add a input event handler with delay to serach for suggestions....
		$(inputElement).on('input', delay(function() {
			if (!autocompleteSearchReady) {
				return; // start only after first key down! (see below)
			}
			// store the current input id
			autocompleteInputID = inputElement.name;
			currentSelectCallback=selectCallback;
			
			// get data options
			var options=$(inputElement).attr('data-options');
			searchCallback({ phrase: this.value, options: options });
		}, 500)).trigger('input');
	
	
		// hide the suggest list on blur event
		$(inputElement).on("blur", delay(function(event) {
			$("[id$=" + $(this).data('resultlist')  + "]").hide();
		}, 200));
	
	
		/*execute a function presses a key on the keyboard:*/
		$(inputElement).keydown(function(e) {
			autocompleteSearchReady=true; // init serach mode
			if (e.keyCode == 40) {
		        /*If the arrow DOWN key is pressed,
		        increase the currentFocus variable:*/
				autocompleteSelectNextElement(this);
			} else if (e.keyCode == 38) { //up
		        /*If the arrow UP key is pressed,
		        decrease the currentFocus variable:*/
				autocompleteSelectPrevElement(this);
			} else if (e.keyCode == 13) {
				/*If the ENTER key is pressed, prevent the form from being submitted,*/
				e.preventDefault();
				selectActiveElement(this);			
			}
		});
	
		// turn autocomplete of
		$(inputElement).attr('autocomplete', 'off');
	},


	/**
	 * Callback method for workiemLink Autocomplete feature
	 */
	addWorkitemRef = function(selection,inputSearchField) {
		
		// find textarea....
		var inputField = $('textarea.workitemlink-textarea-input');
		var list= inputField.val();
		
		var list=inputField.val().split(/\r?\n/);
		var newList= new Array();
		$.each(list, function( key, value ) {
			if (value!='') {
				if (!newList.includes(value)) {
					newList.push(value);
				}  
			}
		});
		if (!newList.includes(selection)) {
			newList.push(selection);
		}  

		var newValue="";
		$.each(newList, function( key, value ) {
			if (key==0) {
				newValue=newValue+value;
			} else {
				newValue=newValue + "\n"+value;
			}
		});

		inputField.val(newValue);
		// trigger on change event
		inputSearchField.trigger('change');
		// clear input
		inputSearchField.val('');			
	},
	
		
	/* Deletes a given $uniqueid from the item $workitemref  */
	deleteWorkitemRef = function(link) {
		
		// find the input field based on the given link.
		var parent=$(link).closest( "span[id$='datalist']" );
		var inputSearchField=$(parent).prevAll('input');
		// find textarea....
		var inputField = $('textarea.workitemlink-textarea-input');
		var workitemref=$(link).data('workitemref');
		
		// only user list is supported 
		if (inputField.is("textarea")) {
			var list=inputField.val().split(/\r?\n/);
			var newList= new Array();
			$.each(list, function( key, value ) {
				if (value!='' && value!=workitemref) {
					if (!newList.includes(value)) {
						newList.push(value);
					}  
				}
			});
			
			var newValue="";
			$.each(newList, function( key, value ) {
				if (key==0) {
					newValue=newValue+value;
				} else {
					newValue=newValue + "\n"+value;
				}
			});

			inputField.val(newValue);
			// trigger on change event
			inputSearchField.trigger('change');
		}
	},


	/**
	* Opens a popup window with the QR-Code to print
    */
	printQRCode=function () {
		var qrCodeWindow = window
				.open(
						imixsOfficeMain.contextPath+"/pages/workitems/qrcode_print.jsf?id="+imixsOfficeMain.workitem_uid,
						"message.print",
						"width=300,height=280,status=no,scrollbars=no,resizable=yes");
		qrCodeWindow.focus();
	};
	

	// public API
	return {
		readCookie : readCookie,
		setCookie : setCookie,
		printQRCode : printQRCode,
		autoPreviewPDF : autoPreviewPDF,
		initSignaturePad : initSignaturePad,
		updateFormWidth : updateFormWidth,
		expandChronicle : expandChronicle,
		shrinkChronicle : shrinkChronicle,
		clearDocumentPreview : clearDocumentPreview,
		updateAttachmentLinks : updateAttachmentLinks,
		showDocument : showDocument,
		toggleChronicleHistory : toggleChronicleHistory,
		toggleChronicleDocuments: toggleChronicleDocuments,
		minimizeDocumentPreview : minimizeDocumentPreview,
		maximizeDocumentPreview : maximizeDocumentPreview,
		closeDocumentPreview : closeDocumentPreview,
		saveWorkitemHandler: saveWorkitemHandler,
		registerSaveWorkitemListener: registerSaveWorkitemListener,
		validateFormMinMaxWidth: validateFormMinMaxWidth,
		workitemRefInitInput: workitemRefInitInput,
		addWorkitemRef: addWorkitemRef,
		deleteWorkitemRef: deleteWorkitemRef,
		
		onFileUploadChange : onFileUploadChange
	};

}());	
	
// Define public namespace
var imixsOfficeWorkitem = IMIXS.org.imixs.workflow.workitem;	
			
	
	