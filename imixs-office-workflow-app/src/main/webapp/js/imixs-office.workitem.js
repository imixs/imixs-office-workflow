"use strict";

// workitem scripts
IMIXS.namespace("org.imixs.workflow.workitem");

var months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug",
	"Sep", "Okt", "Nov", "Dec"];
var documentPreview;				// active document preview element
var documentPreviewIframe;  		// active iFrame
var documentPreviewTypes=".pdf;.jpg;.png;.gif";
var isWorkitemLoading = true; 		// indicates if the workitem is still loading
var chornicleSize = 1;				// default chronicle size (33%)
var chroniclePreview = true; 		// indicates if documents should be shown in the chronicle column
var callbackRegistrySaveWorkitem = [];
var isChronicleResizing = false; 	// tangable divider
var sliderPosX;				    	// current position of moving slider
var workitemElement;
var workitemFormElement;			// the chronicle bar
var workitemSliderElement;
var workitemChronicleElement;

/**
 * Init Method for the workitem page
 * 
 *  - set history nav
 * 
 */
$(document).ready(function () {

	imixsOfficeMain.imixs_document = true; // default
	imixsOfficeMain.imixs_chronicle_comments = true;
	imixsOfficeMain.imixs_chronicle_nav = JSON.parse('{ "comment" : true, "files":true, "version":true, "reference":true }');

	// Init tangible slider
	workitemElement = document.querySelector('.imixs-workitem');
	workitemFormElement = document.querySelector('.imixs-workitem > .imixs-workitem-form');
	workitemChronicleElement = document.querySelector('.imixs-workitem > .imixs-workitem-chronicle');
	workitemSliderElement = document.querySelector('.imixs-workitem > .imixs-slider');
	let lastFormWidth = imixsOfficeWorkitem.readCookie('imixs.office.document.workitem.formwidth');
	// set a default if cookie not yet defined
	if (!lastFormWidth) {
		lastFormWidth = workitemElement.offsetWidth * 0.75;
	}
	lastFormWidth = imixsOfficeWorkitem.validateFormMinMaxWidth(lastFormWidth);
	imixsOfficeWorkitem.updateFormWidth(lastFormWidth);
	if (workitemSliderElement) {
		workitemSliderElement.addEventListener('mousedown', (e) => {
			isChronicleResizing = true;
			sliderPosX = e.clientX;
		});
	}
	window.addEventListener('mouseup', (e) => {
		isChronicleResizing = false;
	});
	window.addEventListener('mousemove', (e) => {
		if (!isChronicleResizing) return;
		let _newWidth = workitemFormElement.offsetWidth + (e.clientX - sliderPosX);
		// adjust minwidth....
		_newWidth = imixsOfficeWorkitem.validateFormMinMaxWidth(_newWidth);
		imixsOfficeWorkitem.updateFormWidth(_newWidth);
		sliderPosX = e.clientX;

	});

	$('.document-nav').hide();

	// Init Document Preview
	documentPreview = $('.imixs-document');
	if (documentPreview) {
		// read cookie
		chroniclePreview = imixsOfficeWorkitem.readCookie('imixs.office.document.chronicle.preview');
		if (chroniclePreview == "false") {
			chroniclePreview = false;
		} else {
			// default true
			chroniclePreview = true;
		}
		documentPreviewIframe = document.getElementById('imixs_document_iframe');
		// the method initAttachmentLinks is called inside the workitem_chronicle_entries for ajax support!
		imixsOfficeWorkitem.autoLoadPreview();
	}

	// Markdown Form Fields
	imixsOfficeWorkitem.initMarkdownItems();

	// Signature Pad
	imixsOfficeWorkitem.initSignaturePad();

	isWorkitemLoading = false;
});



// define core module
IMIXS.org.imixs.workflow.workitem = (function () {
	if (!IMIXS.org.imixs.core) {
		console.error("ERROR - missing dependency: imixs-core.js");
	}

	var imixs = IMIXS.org.imixs.core,

		readCookie = function (name) {
			var nameEQ = name + "=";
			var ca = document.cookie.split(';');
			for (var i = 0; i < ca.length; i++) {
				var c = ca[i];
				while (c.charAt(0) == ' ') c = c.substring(1, c.length);
				if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
			}
			return null;
		},

		setCookie = function (cname, cvalue, exdays = 999) {
			var d = new Date();
			d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
			var expires = "expires=" + d.toUTCString();
			document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/";
		},
		/*
		* This helper method updates all attachment links
		* in the chronicle main view to open documents in the preview window 
		*
		*/
		initAttachmentLinks = function () {
			// update the link action for each file
			// we redirect the href into the iframe target
			var allowedExtensions = documentPreviewTypes.split(';');
			$(".imixs-workitem-chronicle-content a.attachmentlink").each(
				function (index, element) {
					var file_link = $(this).attr('href');
					// update deeplink
					$(this).attr('data-link', file_link);
					// Check if file extension is allowed
					var isAllowed = allowedExtensions.some(function(ext) {
						return file_link.toLowerCase().endsWith(ext);
					});
					if (isAllowed) {
						$(this).click(function () {
							showDocument($(this).text(), file_link);
							// cancel link
							return false;						
						});
					}
				});
		},
		/**
		 * In case the chroniclePreview cookie is set to 'true' the method will 
		 * automatically open the first document.
		 * Documents are only opened in the preview window if the doc type is 
		 * listed in the documentPreviewTypes.
		 */
		autoLoadPreview = function () {
			var showAutoPreview=chroniclePreview;
			// update the link action for each file
			// we redirect the href into the iframe target
			var allowedExtensions = documentPreviewTypes.split(';');
			$(".imixs-workitem-chronicle-content a.attachmentlink").each(
				function (index, element) {
					var file_link = $(this).attr('href');
					// update deeplink
					$(this).attr('data-link', file_link);
					// Check if file extension is allowed
					var isAllowed = allowedExtensions.some(function(ext) {
						return file_link.toLowerCase().endsWith(ext);
					});
					if (isAllowed) {
						// auto prev for first Document only!
						if (showAutoPreview) {
							showDocument($(this).text(), $(this).attr('href'));
							showAutoPreview=false;
						}
					}
				});
		},
		/*
		 * The method opens the document preview window and sets the preview cookie
		 */
		openDocumentPreview = function () {
			if (!documentPreview ) {
				// no prievew frame!
				return;
			}
			//if (chroniclePreview==false) {
				$('.imixs-workitem-form .imixs-form').addClass('split');
				$('.imixs-workitem-form .imixs-document').addClass('split');
				$('.imixs-document').show();
				// set preview cookie
				imixsOfficeWorkitem.setCookie("imixs.office.document.chronicle.preview", "true", 14);
				chroniclePreview=true;
			//}
		},
		/*
		* This method hides the document preview window and removes the preview cookie
		*/
		closeDocumentPreview = function () {
			if (documentPreview) {
				$('.imixs-workitem-form .imixs-form').removeClass('split');
				$('.imixs-workitem-form .imixs-document').removeClass('split');
				$('.imixs-workitem-form .imixs-document').hide();
				// set preview cookie
				imixsOfficeWorkitem.setCookie("imixs.office.document.chronicle.preview", "false", 14);
				chroniclePreview=false;
			}
		},		
		/*
		* This method opens the preview window and loads the current document 
		* into the documentPreviewIframe.
		* The method also updates  the document title in the preview window.
		*/
		showDocument = function (title, link) {
			// alert('show doc');
			if (!documentPreview ) {
				// no prievew frame!
				return;
			}
			if (!link || link == "") {
				return; // no url defined!
			}
			openDocumentPreview();
			// cut title if length >64 chars
			if (title.length > 64) {
				title = title.substring(0, 64) + "...";
			}
			$('.document-title', documentPreview).text(title);
			$('.document-deeplink', documentPreview).attr( 'href',link);
			//documentPreviewURL = link;
			documentPreviewIframe.contentWindow.location.replace(link);		
			$('.document-nav').show();
		},
		downloadDocument = function (element) {
			// find matching deep link
			var file_link = $(element).closest('.content-block').find('.attachmentlink').attr('data-link');
			if (file_link) {
				// Start Download
				//window.location.href = file_link;
				window.open(file_link, '_blank');
			}
		},	

		/** 
		 * This method inits the markdown content for all input fields (type=hidden) with the data-id 'markdown_hidden_input' 
		 * and places a DIV tag with the markdown text. 
		 * The method uses the lib https://marked.js.org/
		 * e.g.:
		 * <item name="invoice.summary" type="custom" path="markdown" />
		 * see: markdown.xhtml
		 */
		initMarkdownItems = function (element) {
			// find all matching input fields
			var markdownInputs = document.querySelectorAll('[data-id="markdown_hidden_input"]');
			markdownInputs.forEach(function(inputElement) {
				// Get the markdown content from the input field
				var mdHtml = marked.parse(inputElement.value);
				// Find the corresponding div - assuming it's the next sibling element
				var divElement = inputElement.nextElementSibling;
				if (divElement && divElement.classList.contains('imixs-markdown-output')) {
					divElement.innerHTML = mdHtml;
				}
			});
		},	

		/*
		 * This method initializes a signature pad if part of the form.
		 * The signature widget supports a 2d canvas to draw a signature
		 * with the mouse cursor. 
		 */
		initSignaturePad = function () {
			const signatureCanvas = document.querySelector('.imixs-signature-pad canvas');
			if (!signatureCanvas) {
				// no signature pad available
				return;
			}
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

			signatureCanvas.addEventListener('pointerup', (event) => {
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
						nextElement.value = imageURL;
						break;
					}
					nextElement = nextElement.nextElementSibling;
				}
			})
		},


		/*
		 * This helper method adjusts the minimum/maximum 
		 * width of the from view
		 */
		validateFormMinMaxWidth = function (_width) {
			// adjust minwidth....
			if (_width < 500) {
				_width = 500;
				isChronicleResizing = false;
			}
			if (_width > workitemElement.offsetWidth - 340) {
				_width = workitemElement.offsetWidth - 340;
				isChronicleResizing = false;
			}
			return _width;
		},


		/*
		 * updates the workitem form width and update the corresponding cookie
		 */
		updateFormWidth = function (_newWidth) {
			if (workitemSliderElement) {
				const chronicleWidth = workitemElement.offsetWidth - _newWidth - workitemSliderElement.offsetWidth;
				workitemChronicleElement.style.flexBasis = `${chronicleWidth}px`;
				imixsOfficeWorkitem.setCookie("imixs.office.document.workitem.formwidth", _newWidth, 14);
			}
		},

		/*
		 * reduce the with of the chronicle
		 */
		expandChronicle = function () {
			let newSize = validateFormMinMaxWidth(workitemElement.offsetWidth / 2);
			imixsOfficeWorkitem.updateFormWidth(newSize);
		},

		/*
		 * increase the with of the chronicle
		 */
		shrinkChronicle = function () {
			let newSize = validateFormMinMaxWidth(workitemElement.offsetWidth - 340);
			imixsOfficeWorkitem.updateFormWidth(newSize);
		},


		/*
		 * This method toggles into the chronicle documents view
		 */
		toggleChronicleHistory = function () {
			$('.chronicle-tab-history').parent().addClass('active');
			$('.chronicle-tab-documents').parent().removeClass('active');
			$('.chronicle-tab-ai').parent().removeClass('active');
			$('#imixs-workitem-chronicle-tab-documents').hide();
			$('#imixs-workitem-chronicle-tab-ai').hide();
			$('#imixs-workitem-chronicle-tab-history').show();
			// set a right margin for history view only
			$('.imixs-workitem-chronicle-content').css('width', 'calc(100% - 30px)');
		},
		toggleChronicleAI = function () {
			$('.chronicle-tab-ai').parent().addClass('active');
			$('.chronicle-tab-history').parent().removeClass('active');
			$('.chronicle-tab-documents').parent().removeClass('active');
			$('#imixs-workitem-chronicle-tab-history').hide();
			$('#imixs-workitem-chronicle-tab-documents').hide();
			$('#imixs-workitem-chronicle-tab-ai').show();
			// set a right margin for history view only
			$('.imixs-workitem-chronicle-content').css('width', 'calc(100% - 0px)');
		},


		registerSaveWorkitemListener = function (callback) {
			callbackRegistrySaveWorkitem.push(callback);
		},

		/*
		 * Helper method handles registered callback methods
		 */
		saveWorkitemHandler = function (confirmMessage, uiWorkflowAction) {
			if (confirmMessage) {
				if (confirm(confirmMessage) == false) {
					return false;
				}
			}
			// do we have callbacks?
			if (callbackRegistrySaveWorkitem) {
				for (const saveCallback of callbackRegistrySaveWorkitem) {
					var callBackResult = saveCallback(uiWorkflowAction);
					if (callBackResult === false) {
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
		workitemRefInitInput = function (inputElement, searchCallback, resultlistId, selectCallback) {

			// set id for result list element
			if (!resultlistId || resultlistId === '') {
				resultlistId = 'autocomplete-resultlist'; // default name
			}
			$(inputElement).attr('data-resultlist', resultlistId);

			// add a input event handler with delay to serach for suggestions....
			$(inputElement).on('input', delay(function () {
				if (!autocompleteSearchReady) {
					return; // start only after first key down! (see below)
				}
				// store the current input id
				autocompleteInputID = inputElement.name;
				currentSelectCallback = selectCallback;

				// get data options
				var options = $(inputElement).attr('data-options');
				searchCallback({ phrase: this.value, options: options });
			}, 500)).trigger('input');


			// hide the suggest list on blur event
			$(inputElement).on("blur", delay(function (event) {
				$("[id$=" + $(this).data('resultlist') + "]").hide();
			}, 200));


			/*execute a function presses a key on the keyboard:*/
			$(inputElement).keydown(function (e) {
				autocompleteSearchReady = true; // init serach mode
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
		addWorkitemRef = function (selection, inputSearchField) {
			// find textarea....
			var inputField = $('textarea.workitemlink-textarea-input');
			var itemInput = $('textarea', inputSearchField.parent());
			var list = inputField.val();
			var list = inputField.val().split(/\r?\n/);
			var newList = new Array();
			$.each(list, function (key, value) {
				if (value != '') {
					if (!newList.includes(value)) {
						newList.push(value);
					}
				}
			});
			if (!newList.includes(selection)) {
				newList.push(selection);
			}

			var newValue = "";
			$.each(newList, function (key, value) {
				if (key == 0) {
					newValue = newValue + value;
				} else {
					newValue = newValue + "\n" + value;
				}
			});

			inputField.val(newValue);

			// clear input
			inputSearchField.val('');

			// update the item name (find next textarea)
			if (itemInput && itemInput.length == 1) {
				var oldItemValue = itemInput.val();
				if (oldItemValue === '') {
					itemInput.val(selection);
				} else {
					if (itemInput.val().indexOf(selection) == -1) {
						itemInput.val(itemInput.val() + '\n' + selection);
					}
				}
			}

			// finally trigger on change event
			inputSearchField.trigger('change');

		},


		/* Deletes a given $uniqueid from the item $workitemref  */
		deleteWorkitemRef = function (link) {

			// find the input field based on the given link.
			var parent = $(link).closest("span[id$='datalist']");
			var inputSearchField = $(parent).prevAll('input');
			var itemInput = $('textarea', inputSearchField.parent());
			// find textarea....
			var inputField = $('textarea.workitemlink-textarea-input');
			var workitemref = $(link).data('workitemref');

			// only multi list is supported 
			if (inputField.is("textarea")) {
				var list = inputField.val().split(/\r?\n/);
				var newList = new Array();
				$.each(list, function (key, value) {
					if (value != '' && value != workitemref) {
						if (!newList.includes(value)) {
							newList.push(value);
						}
					}
				});

				var newValue = "";
				$.each(newList, function (key, value) {
					if (key == 0) {
						newValue = newValue + value;
					} else {
						newValue = newValue + "\n" + value;
					}
				});

				inputField.val(newValue);

				// update the item name (find next textarea)
				if (itemInput && itemInput.length == 1) {
					var itemUpdateValue = itemInput.val();
					if (itemUpdateValue.indexOf(workitemref) > -1) {
						itemUpdateValue = itemUpdateValue.replace(workitemref + "\n", "");
						itemUpdateValue = itemUpdateValue.replace(workitemref, "");
						itemInput.val(itemUpdateValue);
					}
					itemInput.trigger('change');
				}

				// finally trigger on change event
				inputSearchField.trigger('change');


			}
		},


		/**
		* Opens a popup window with the QR-Code to print
		*/
		printQRCode = function () {
			var qrCodeWindow = window
				.open(
					imixsOfficeMain.contextPath + "/pages/workitems/qrcode_print.jsf?id=" + imixsOfficeMain.workitem_uid,
					"message.print",
					"width=300,height=280,status=no,scrollbars=no,resizable=yes");
			qrCodeWindow.focus();
		},

		/**
		 * Format IBAN in 4-digit blocks
		 */
		formatIBAN = function (inputElement) {
			let value = inputElement.value.replace(/ /g, ''); // Entferne vorhandene Leerzeichen
			let formattedIBAN = '';
			for (let i = 0; i < value.length; i += 4) {
				let block = value.slice(i, i + 4);
				formattedIBAN += block + ' ';
			}
			inputElement.value = formattedIBAN.trim().toUpperCase(); // Entferne Leerzeichen am Ende
		}

	


	// public API
	return {
		readCookie: readCookie,
		setCookie: setCookie,
		printQRCode: printQRCode,
		initAttachmentLinks: initAttachmentLinks,
		autoLoadPreview: autoLoadPreview,
		openDocumentPreview: openDocumentPreview,
		closeDocumentPreview: closeDocumentPreview,
		showDocument: showDocument,
		downloadDocument: downloadDocument,
		initSignaturePad: initSignaturePad,
		updateFormWidth: updateFormWidth,
		expandChronicle: expandChronicle,
		shrinkChronicle: shrinkChronicle,
		toggleChronicleHistory: toggleChronicleHistory,
		toggleChronicleAI: toggleChronicleAI,
		saveWorkitemHandler: saveWorkitemHandler,
		registerSaveWorkitemListener: registerSaveWorkitemListener,
		validateFormMinMaxWidth: validateFormMinMaxWidth,
		workitemRefInitInput: workitemRefInitInput,
		addWorkitemRef: addWorkitemRef,
		deleteWorkitemRef: deleteWorkitemRef,
		initMarkdownItems: initMarkdownItems,
		formatIBAN: formatIBAN
	};

}());

// Define public namespace
var imixsOfficeWorkitem = IMIXS.org.imixs.workflow.workitem;


