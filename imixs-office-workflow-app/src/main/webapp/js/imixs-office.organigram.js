"use strict";

// workitem scripts

IMIXS.namespace("org.imixs.workflow.organigram");

	var datasource_processes;
	var datasource_spaces;
	var direction;
	var verticalLevel;
	var ocSpaces;
	var ocProcesses; 
	var selectedNodeDetails;

/**
 * Init Method for the organinsation page
 * 
 */
$(document).ready(function() {
	
	ocProcesses=$('#organigram-processes').orgchart({
		'data' : datasource_processes,
		'nodeContent' : 'title',
		'verticalLevel': 2
	});
	
	ocSpaces=$('#organigram-spaces').orgchart({
		'data' : datasource_spaces,
		'nodeContent' : 'title',
		'direction' : direction,
		'verticalLevel' : verticalLevel,
		'toggleSiblingsResp' : false,
        'pan': true,
        'zoom': true
	});
	
	// on click event handler for space nodes and process nodes only
	ocSpaces.$chartContainer.on('click', '.node.space',toggleTeamlist);
	ocProcesses.$chartContainer.on('click', '.node.process',toggleTeamlist);
	
	
	// add a create link for the root nodes...
	var linkProcess="<div class='node-details'><div class='controls'><a href='/pages/admin/process.xhtml?id=' ><span class='typcn typcn-folder-add'></span></a></div></div>";
	var linkSpace="<div class='node-details'><div class='controls'><a href='/pages/admin/space.xhtml?id=' ><span class='typcn typcn-folder-add'></span></a></div></div>";
	$("#root .content","#organigram-processes").append( linkProcess );
	$("#root .content","#organigram-spaces").append( linkSpace );
	
	

	// zoom function
    ocSpaces.$chartContainer.on('touchmove', function(event) {
      event.preventDefault();
    });


});


/*
 * This method toggles the node details. The method calls a ajax method to trigger the CDI Bean organigramController.
 * The method updates the hidden input field selected_node_id with the current node UniqueID. 
 * This enables the organigramController CDI Bean loads the node details.  
 */
function toggleTeamlist() {
	var $this = $(this);
	var id=$this.attr('id');
	var inputSelectedNodeID=  $("input[id$='selected_node_id']")
	inputSelectedNodeID.val(id);
	
	// get content element of the selected node
    selectedNodeDetails=$(".content",$this);

    // toggle team list
    var nodeDetails=$(selectedNodeDetails).find('.node-details');
    if (nodeDetails && nodeDetails.length>0) {
    	// clean team list if available
    	$(nodeDetails).remove();
    } else {
		// trigger the ajax method to load the details template...
		loadTeamDetails();
    }
}

/*
 * This methods copies the node details into the selcted node
 * The method is called asynchronious from the scriptCommand in the organisation.xhtml page 
 */
function showNodeDetails() {
	if (selectedNodeDetails) {
		// copy the node details from the node-details-template into the current node....
		var html=$(".node-details","span[id$='node-details-template']")  
		$(selectedNodeDetails).append( html );
	}
}


/**
 * Open the edit form for a space or process
 */
function editNode(id,className) {
	//http://localhost:8080/pages/admin/space.jsf?id=aa8fbc5c-34b4-46b2-8ec0-773b64159b86
	if (className=="space") {
		document.location.href="/pages/admin/space.jsf?id="+id;
	} 
	if (className=="process") {
		document.location.href="/pages/admin/process.jsf?id="+id;
	} 
}
function addNode(id) {
	//http://localhost:8080/pages/admin/space.jsf?id=aa8fbc5c-34b4-46b2-8ec0-773b64159b86
	//document.location.href="/pages/admin/space.jsf?id="+id;
	// TODO
}


/**
 * Helper method to search a node by id in a given node
 * The methdo is called recursiv on embedded child nodes.  
 */
function findNodeById(node,id) {
	// get child structure
	
	if (node.id==id) {
		// match!
		return node;
	}
	
	// check childs....
	var childs=node.children;
	if (childs && childs.length>0) {
		for (let subNode of childs) {
   		 	// Do something
			if (subNode.id==id) {
				// match!
				return subNode;
			} else {
				// recursive call with childs
				var subResult = findNodeById(subNode,id);
				if (subResult) {
					return subResult;
				}
			}
		}
		// no match
	}
}
			


// Define public namespace
var imixsOfficeOrganigram = IMIXS.org.imixs.workflow.organigram;	
			
	
	