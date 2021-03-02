"use strict";

// workitem scripts

IMIXS.namespace("org.imixs.workflow.organigram");

	var datasource_processes;
	var datasource_spaces;
	var direction;
	var verticalLevel;
	var ocSpaces;
	var ocProcesses; 

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

	// zoom function
    ocSpaces.$chartContainer.on('touchmove', function(event) {
      event.preventDefault();
    });


});

/*
 * This method toggles the team list members of a node 
 */
function toggleTeamlist() {
	var $this = $(this);
	var id=$this.attr('id');
	var className = $this.attr('class');
	
    // show teamlist only for non-root nodes
	var rootNode;
	if (className.indexOf('process')>-1) {
		rootNode=ocProcesses.opts.data;
	} else {
		rootNode=ocSpaces.opts.data;
	}
      
     // get content element of selected node
     var contentNode=$(".content",$this);
     // toggle team list
     var teamlist=$(contentNode).find('.content-custom');
     if (teamlist && teamlist.length>0) {
    	// clean team list if available
    	$(teamlist).remove();
     } else {
    	// find the selected node in the organigram...
	    var node = findNodeById(rootNode,id);
    	if (node) {
			$(contentNode).append( buildCustomContentTable(node) );
    	}
        
     }
}

/*
 * Helper method to build a team table element within the content div
 */
function buildCustomContentTable(node) {
	
	
	var html="<div class='content-custom'><div class='teams'>";
	
	// build manager list
	html=html+"<div class='members'><p class='lead'><strong>Manager</strong></p>";
	for (let member of node.manager) {
		html=html+"<p>" + member + "</p>";
	}
	html=html+"</div>";

	
	// build team list
	html=html+"<div class='members'><p class='lead'><strong>Team</strong></p>";
	for (let member of node.team) {
		html=html+"<p>" + member + "</p>";
	}
	html=html+"</div>";
	
	// build assist list
	html=html+"<div class='members'><p class='lead'><strong>Assistenz</strong></p>";
	for (let member of node.assist) {
		html=html+"<p>" + member + "</p>";
	}
	html=html+"</div>";
	
	
	
	// build footer with edit and add symbols....
	html=html+"</div>"
	
	
	
	html=html + "<div class='controls'><span class='typcn typcn-edit' onclick='editNode(&quot;" + node.id +"&quot;,&quot;" + node.className + "&quot;);'></span>";
	if (node.className=="space") {
		// add sub space...
		html=html + "<span class='typcn typcn-flow-children'  onclick='addNode(&quot;" + node.id +"&quot;);'></span>";
	}
	html=html + "</div>";
	
	
	
	html=html+"</div>"
	return html;
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
			
	
	