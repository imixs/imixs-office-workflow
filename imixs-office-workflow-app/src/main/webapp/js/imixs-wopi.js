"use strict";

IMIXS.namespace("org.imixs.workflow.wopi");

/**
 * Imixs-Wopi Adapter Script for the wopi integration
 * 
 * We register a message listener to react on messages send form the Editor to the host.
 * We send the 'Host_PostmessageReady' message to signal the Editor that we want receive all action messages.
 * 
 * We also customize the toolbar in the following way: 
 *  - hide the defult save command
 *  - add a custom save button with 'notify=true'
 *  - add a custom close button with 'notify=true'
 *  - privide event listener methods for the custom save/close actions
 * 
 */

// add a event listner to receife messages from the Wopi Editor
$(document).ready(function() {
	// Install the wopi message listener.
	// receive messages form libreoffice online
	window.addEventListener("message", imixsWopi.receiveMessage, false);
	
	// because of a bug in collabora we need to construct a dummy cookie here
	// https://github.com/CollaboraOnline/online/issues/2380
	var expireDate = new Date();
	expireDate.setHours(expireDate.getHours() + 24);	
	var topDomain=window.location.hostname;
	//console.log("...domain=" + topDomain);
	var domainParts = topDomain.split('.');
	if (domainParts && domainParts.length>1) {
		// extract top domain (last to parts)
	   topDomain='.'+domainParts[domainParts.length-2] +'.'+domainParts[domainParts.length-1];
	   //console.log("..top domain = " + topDomain);
	}
	document.cookie = "imixs_wopi_dummy=true;expires=" + expireDate 
	                  + ";domain=" + topDomain+";path=/";
    // we hope that we can remove this cooie hack if issue 2380 is fixed
});

// The imixsWopi core module
// Provide method for UI integration
IMIXS.org.imixs.workflow.wopi = (function() {
	if (!IMIXS.org.imixs.core) {
		console.error("ERROR - missing dependency: imixs-core.js");
	}
	var imixs = IMIXS.org.imixs.core,

		viewerID = "",
		saveCallback = null,	
		isModified = false,	
		filename = filename,
		
		// Receive Editor Messages
		// This function is invoked when the editor posts a message back.
		// We react on 'Document_Loaded' to send a Host_PostmessageReady and we react on 
		// Save and Custom Click events
		receiveMessage = function(event) {
			if (!event.data) {
				return;
			}
			
			var msg =null;
			// we only accept events with data string starting with "MessageId"
			if (Object.prototype.toString.call(event.data) != "[object String]") {
				msg=event.data;
			} else {
				// parse....
				msg = JSON.parse(event.data);
			}
			if (!msg) {
				return;
			}
			//console.log('==== framed.doc.html receiveMessage: ' + event.data);
			
			if (msg.MessageId == 'App_LoadingStatus') {
				if (msg.Values) {
					if (msg.Values.Status && msg.Values.Status == 'Document_Loaded') {
						//console.log('==== Document loaded ...init viewer...');
						initViewer();
						imixsWopi.isModified=false;
					}
				}
				
			} else if (msg.MessageId == 'Doc_ModifiedStatus') {
				if (msg.Values) {
					imixsWopi.isModified=msg.Values.Modified;
					/*if (msg.Values.Modified == true) {
						//console.log('====  document modified.');
						imixsWopi.isModified=true;
					}*/
				}				
			// custom click events
			} else if (msg.MessageId == 'Clicked_Button') {
				if (msg.Values && msg.Values.Id=="imixs.save") {
					//console.log('====  imixs.save');
					postMessage({
						'MessageId': 'Action_Save',
						'Values': { 'Notify': true }
					});
					//save();
				} else if (msg.Values && msg.Values.Id=="imixs.close") {
					//console.log('====  imixs.close');
					imixsWopi.closeViewer();
				}
			// action save completed 
			} else if (msg.MessageId == 'Action_Save_Resp') {
				if (msg.Values) {
					if (msg.Values.success == true) {
						//console.log('==== Saved');
						imixsWopi.isModified=false;
						if (imixsWopi.saveCallback) {
							imixsWopi.saveCallback(imixsWopi.filename);
						}
					} else {
						console.log('==== Error during save');
					}
				}
			}
			
		},
		
		// send a message to the Editor to customize the behaviour
		postMessage = function(msg) {
			//console.log(msg);
			var iframe = document.getElementById('wopi-iframe');
			iframe = iframe.contentWindow || (iframe.contentDocument.document || iframe.contentDocument);						
			iframe.postMessage(JSON.stringify(msg), '*');
		},

		/* This is the main method to lauch the office editor.
		 * The method switch to iframe mode and loads the editor.
		 * The content of the iframe is constructed by the method 
		 * buildViewer();
		 */
		openViewer = function(viewerID,ref,filename) {
			imixsWopi.viewerID=viewerID;
			imixsWopi.filename=filename;
			var wopiuri = ref;			
			var wopiViewer = $('#' + imixsWopi.viewerID);
			wopiViewer.show();
			buildViewer(imixsWopi.viewerID,wopiuri);			
			var form = $('#wopi-iframe').contents().find('#libreoffice-form');
			form.submit();
		},

		/*
		 * This helper method builds a iframe with a form at a given 
		 * element. This is used to show the Office editor later.
		 * The form has two hidden input fields 'access_token' and
		 * 'access_token_ttl'. The token is extracted from the actionUri 
		 *
		 */
		buildViewer = function(elementid, actionuri) {
			var iframeElement = $("#" + elementid);
			$(iframeElement).empty();
			// build iframe....
			var style_attr = 'width: 100%;height: 800px;margin: 0;border: none;display: block;';
			var sandbox_attr ='allow-scripts allow-same-origin allow-forms allow-popups allow-top-navigation allow-popups-to-escape-sandbox allow-downloads allow-modals';
			var allow_attr='autoplay camera microphone display-capture';
			 
	        // extract token from uri
	        var tokenPos=actionuri.indexOf('access_token=');
	        var access_token=actionuri.substring(tokenPos+13);
	        // console.log(' actionuri='+actionuri);	        
	        // console.log(' access_token='+access_token);	     
	        actionuri=actionuri.substring(0,tokenPos);   
			// console.log(' short actionuri='+actionuri);	        
			
			
			/* Old Construction */
			var content = '<iframe id="wopi-iframe" src="" allow="' + allow_attr + '" style="' + style_attr + ' allowfullscreen="true" sendbox="' + sandbox_attr + '" title="Office"></iframe>';
			$(iframeElement).append(content);
			var iframe = document.getElementById('wopi-iframe');
			iframe = iframe.contentWindow || (iframe.contentDocument.document || iframe.contentDocument);
			iframe.document.open();
			var uiHiddenFields = '<input name="access_token" value="'+access_token+'" type="text" /><input name="access_token_ttl" value="0" type="text" />';
			// uimode hack: https://sdk.collaboraonline.com/docs/theming.html
			var uiModeCollabora = '<input name="ui_defaults" value="TextSidebar=false;SpreadsheetSidebar=false" type="hidden"/>';
			iframe.document.write('<html><body><form action="'+actionuri+'" enctype="multipart/form-data" method="post" id="libreoffice-form" style="display:none;">' +uiHiddenFields+ uiModeCollabora + '<input type="submit" value="Load..." /></form></body></html>');
			iframe.document.close();
		},

		// close the office viewer and show the form part again
		closeViewer = function() {		
			var wopiviewer = $('#' + imixsWopi.viewerID);
			$(wopiviewer).empty();
			wopiviewer.hide();
		},
		
		switchToFullScreen = function() {	
			var iframe = document.getElementById('wopi-iframe');
		    // Do fullscreen
		    if (iframe.requestFullscreen) {
		      iframe.requestFullscreen();
		    } else if (iframe.webkitRequestFullscreen) {
		      iframe.webkitRequestFullscreen();
		    } else if (iframe.mozRequestFullScreen) {
		      iframe.mozRequestFullScreen();
		    } else if (iframe.msRequestFullscreen) {
		      iframe.msRequestFullscreen();
		    }
		},
		
		// method to initalize the PostMessage communication
		// and to customize the editor
		initViewer = function() {
			var iframe = document.getElementById('wopi-iframe');
				iframe = iframe.contentWindow || (iframe.contentDocument.document || iframe.contentDocument);

				iframe.postMessage(JSON.stringify({ 'MessageId': 'Host_PostmessageReady' }), '*');
				//window.frames[0].postMessage(JSON.stringify({ 'MessageId': 'Host_PostmessageReady' }), '*');
				//console.log('==== Host_PostmessageReady message send....');
				
				// hide default UI_save button
				/*imixsWopi.postMessage({
					'MessageId' : 'Hide_Button',
					'Values' : {
						'id' : "save"
					}
				});	*/
			
			},
		
		// sends a post mesage to save the document
		save = function() {
			var date=Date.now();
			//console.log('external save '+date);
			postMessage({
				"MessageId" : "Action_Save",
				//"SendTime" :  date,
				"Values" : {
					"DontTerminateEdit" : true,
					"DontSaveIfUnmodified": false,
					"Notify" : true
				}
			});
		};

	// public API
	return {
		viewerID: viewerID,
		filename: filename,
		receiveMessage: receiveMessage,
		postMessage: postMessage,
		openViewer: openViewer,
		closeViewer: closeViewer,
		isModified: isModified,
		save: save,
		switchToFullScreen: switchToFullScreen,
		saveCallback: saveCallback
	};

}());

// Define public namespace
var imixsWopi = IMIXS.org.imixs.workflow.wopi;
