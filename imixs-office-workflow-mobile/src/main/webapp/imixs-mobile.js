/*! Imixs Office Workflow - mobile v0.0.1 */

// Bind mobileinit event
$(document).bind("mobileinit", function() {

	setupWorkflowService("http://localhost:8080/office-rest/", "", 1);
	// alert('binding..');
	// overwrite configuration if necessarry
	$.extend($.mobile, {
	// config...

	});



	// register custom refreshWorklist Event Handler for #worklist
	$(document).on('pageinit', '#worklist', function(event, ui) {
		$(document).on('refreshWorklist','#worklist', function(e, adata, aservice) {
			alert('jetzt male ich23');

		});
	});

	// Event Handler for process page on pageshow event
	$(document).on('pageshow', '#worklist', function(event, ui) {
		alert('pageshow worklist hh');
		// clearView();
		// fetchWorklist();

		loadWorkitems('workflow/worklistbyowner/null.json','#worklist');
	});

});

// ########### Worklist ###################

// this method clears the worklist from the local cache and
// removes the view body
function clearWorklist(view_type) {
	// alert('clearView....');
	localStorage.removeItem("com.imixs.workflowgroups." + view_type);
	$("#worklist_view").empty();

}

// this method fills the view body with the entries
// view_type indicates the report (e.g. mobile-worklist,
// mobile-processworklist,...)
// the workitems are stored into an array containing the json object of the
// workitem
function fetchWorklist() {

	// read worklist options..
	var service = $.getURLParam("service");

	// alert(view_type);
	// we first test if we still have the workflow groups in the local storage
	var data = localStorage.getItem("com.imixs.worklist." + service);
	if (data == null) {
		// create the list of workitems using the RESTfull service interface

		// alert('starting json request ....');
		$
				.getJSON(
						"/office-mobile/rest/" + service + "?",
						{
							count : "30",
							start : "0",
							items : "$uniqueid,txtworkflowgroup,txtworkflowstatus,txtworkflowsummary,txtworkflowimageurl,namcurrenteditor,$modified"
						}, function(data) {
							// alert('REST Service: worklist loaded....');
							// json request finished store the worklist.....
							localStorage.setItem("com.imixs.worklist."
									+ service, JSON.stringify(data));
							writeWorklist(service);

						});

	} else {
		// use workflow groups from cache
		// alert("liste from cache=" + vWorkflowGroups);
		writeWorklist(service);
	}
}

// this method writes the html code for the workflow group list..
function writeWorklist(service) {
	// alert('Write worklist...');

	dataString = localStorage.getItem("com.imixs.worklist." + service);
	var data = JSON.parse(dataString);

	if (data == null) {
		$("#worklist_view").append("<li>Keine Daten vorhanden</li>");
		// refresh view
		$("#worklist_view").listview("refresh");
		return;
	}

	// check if the entity is an array ...
	if (data.entity[0] == null) {
		// need to be optimized - see duplicate code

		// wie macht man aus einem data.entity ein array??
		var workitem = data.entity;
		var img = "<img src=\"" + workitem.item[4].value.$ + "\" />";
		var p_status = "<p><b>" + workitem.item[1].value.$ + "</b>: "
				+ workitem.item[2].value.$ + "</p>";
		$("#worklist_view").append(
				"<li>" + "<a href=\"workitem.html?id="
						+ workitem.item[0].value.$ + "\">" + img + "<h3>"
						+ workitem.item[3].value.$ + "</h3>" + p_status
						+ "</a></li>");

	} else
		// iterate over all workitems
		$.each(data.entity, function(i, workitem) {

			var img = "<img src=\"" + workitem.item[4].value.$ + "\" />";
			var p_status = "<p><b>" + workitem.item[1].value.$ + "</b>: "
					+ workitem.item[2].value.$ + "</p>";
			$("#worklist_view").append(
					"<li>" + "<a href=\"workitem.html?id="
							+ workitem.item[0].value.$ + "\">" + img + "<h3>"
							+ workitem.item[3].value.$ + "</h3>" + p_status
							+ "</a></li>");

		});

	// refresh view
	$("#worklist_view").listview("refresh");
}

// ############## Workitem ####################

// this method fills the view body with the entries of a single workitem
// id indicates the $uniqueid of the workitem
// the method tries to load the workitem from an internal cache
function fetchWorkitem() {

	// read worklist options..
	var uid = $.getURLParam("id");

	// we first test if we still have the workflow groups in the local storage
	var data = localStorage.getItem("com.imixs.workitem." + uid);
	if (data == null) {
		// create the list of workitems using the RESTfull service interface

		// alert('starting json request ....');
		$
				.getJSON(
						"/office-mobile/rest/workflow/workitem/" + uid
								+ ".json?",
						{
							items : "$uniqueid,txtworkflowgroup,txtworkflowstatus,txtworkflowsummary,txtworkflowimageurl,namcurrenteditor,$modified,txtworkflowabstract"
						}, function(data) {
							// alert('REST Service: worklist loaded....');
							// json request finished store the worklist.....
							localStorage.setItem("com.imixs.workitem." + uid,
									JSON.stringify(data));
							writeWorkitem(uid);
						});

	} else {
		// use workflow groups from cache
		// alert("liste from cache=" + vWorkflowGroups);
		writeWorkitem(uid);
	}
}

// this method writes the html code for the workflow group list..
function writeWorkitem(uid) {

	dataString = localStorage.getItem("com.imixs.workitem." + uid);
	var workitem = JSON.parse(dataString);

	var img = "<img src=\"" + workitem.item[4].value.$
			+ "\" style=\"margin-right:8px;\"/>";

	// alert(workitem.item[7].value.$) ;

	$("#workitem_summary").append(img + workitem.item[3].value.$);

	$("#workitem_abstract").append(workitem.item[7].value.$);
	$("#workitem_workflowgroup").append(workitem.item[1].value.$);
	$("#workitem_workflowstatus").append(workitem.item[2].value.$);
	$("#workitem_currenteditor").append(workitem.item[5].value.$);
	$("#workitem_modified").append(workitem.item[5].value.$);

	// refresh view
	$("#workitem_view").listview("refresh");
}

// ########### Projectlist ###################

// this method clears the project from the local cache and
// removes the view body
function clearProjects() {
	// alert('clearView....');
	localStorage.removeItem("com.imixs.projects");
	$("#projects_view").empty();

}

// this method fills the view body with the entries
// view_type indicates the report (e.g. mobile-worklist,
// mobile-processworklist,...)
// the workitems are stored into an array containing the json object of the
// workitem
function fetchProjects() {

	// we first test if we still have the workflow groups in the local storage
	var data = localStorage.getItem("com.imixs.projects");
	if (data == null) {
		// create the list of workitems using the RESTfull service interface

		// alert('starting json request ....');
		$.getJSON("/office-mobile/rest/report/mobile-projects.json?", {
			count : "30",
			start : "0"
		}, function(data) {
			// alert('REST Service: worklist loaded....');
			// json request finished store the worklist.....
			localStorage.setItem("com.imixs.projects", JSON.stringify(data));
			writeProjects();
		});

	} else {
		// use workflow groups from cache
		// alert("liste from cache=" + vWorkflowGroups);
		writeProjects();
	}
}

// this method writes the html code for the workflow group list..
function writeProjects() {
	// alert('Write worklist...');

	dataString = localStorage.getItem("com.imixs.projects");
	var data = JSON.parse(dataString);
	// iterate over all workitems
	$.each(data.entity, function(i, workitem) {

		// alert("Schei workitem.length = " + workitem.length);
		var name = "";
		var description = "";
		var uid = "";
		// iterate over all items and find the workflowgroup and date items....
		for ( var j = 0; j < workitem.item.length; j++) {
			// alert("item"+j+ "= "+workitem.item[j].name);
			if (workitem.item[j].name == "txtname")
				name = workitem.item[j].value.$;
			if (workitem.item[j].name == "txtdescription")
				description = workitem.item[j].value.$;
			if (workitem.item[j].name == "$uniqueid")
				uid = workitem.item[j].value.$;

		}

		$("#projects_view").append(
				"<li><a href=\"worklist.html?service=workflow/worklistbyref/"
						+ uid + ".json" + "\">" + name + "</a></li>");

	});
	// refresh view
	$("#projects_view").listview("refresh");
}

// ########### Workflow Groups ###################

// this method clears the workflow groups from the local cache and
// removes the view body
function clearWorkflowGroups() {
	// alert('clearView....');
	localStorage.removeItem("com.imixs.workflowgroups");
	$("#workflowgroup_view").empty();

}

// this method fills the view body with the entries
function fetchWorkflowGroups() {

	// we first test if we still have the workflow groups in the local storage
	// alert(" buildView....");
	// localStorage.clear();
	var vWorkflowGroups = localStorage.getItem("com.imixs.workflowgroups");
	if (vWorkflowGroups == null) {
		// create the list of workfow groups using the RESTfull service
		// interface
		vWorkflowGroups = new Array();

		// alert('starting json request ....');
		$.getJSON("/office-mobile/rest/model/groups/office-de-0.0.2.json?", {
			items : "txtworkflowgroup,txtname"
		}, function(data) {
			// alert('REST Service: Data loaded....');

			// iterate over all workitems and store only those workflowgroups
			// into a
			// array with have no '~' in there groupname
			$.each(data.entity, function(i, workitem) {
				var workflowGroup = workitem.item[0].value.$;
				// test if gropname has a '~'
				if (workflowGroup.indexOf('~') == -1)
					vWorkflowGroups.push(workitem);
			});

			// json request finished
			// store the workflow groups.....
			localStorage.setItem("com.imixs.workflowgroups", JSON
					.stringify(vWorkflowGroups));
			writeWorkflowGroups();

		});

	} else {
		// use workflow groups from cache
		// alert("liste from cache=" + vWorkflowGroups);
		writeWorkflowGroups();
	}
}

// this method writes the html code for the workflow group list..
function writeWorkflowGroups() {

	dataString = localStorage.getItem("com.imixs.workflowgroups");
	var data = JSON.parse(dataString);
	// iterate over all workitems
	$.each(data, function(i, workitem) {

		// create link
		// alert(groups[j]);
		var link = "worklist.html?service=workflow/worklistbygroup/"
				+ workitem.item[0].value.$ + ".json";

		$("#workflowgroup_view").append(
				"<li><a href=\"" + link + "\">" + workitem.item[0].value.$
						+ "</a></li>");

	});

	// refresh view
	$("#workflowgroup_view").listview("refresh");
}
