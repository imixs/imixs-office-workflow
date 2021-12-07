# The MinuteController

The backing Bean _org.imixs.marty.web.workitem.MinuteController_ provides functionality to display miute forms.
A minute form consists of a header for general information and a body part to display an manage minutes. 
 
 See also the [MinutePlugin](../pluginsr/minuteplugin.html)

## Name Fields

A minute Workflow distinguish the following name fields. 

** Header **

 * \_team = a team list for the parent minute
 * \_present = a list of attendees   
 * namprocessteam = the process team
 * $editor = keeper of the minutes
 

** Minute **

 * \_team = the team list form the header (copied by the splitplugin)
 * \_present = the  attendees form the header (copied by the splitplugin)
 * \_minute\_team = a team list for a single minute
 * namprocessteam = the process team
 * namspaceteam = optional space team.


## Forms

The project [Imixs-Office-Workflow](http://www.office-workflow.de) provides a set of forms to display minutes. 
These forms a gerneric and can be combined with any other form type. 


### The Minute Body
The minute body form _minutes/body_ displays all minutes contained in a workitem. The content of a minute is displayed by its $WorkflowAbstract. 
A user can open a minute to edit the content of a minute with the underling form parts defined by the corresponding model.  

A disable the feature for in-line-editing a single minute, the property '_minuteslocked_' of the parent workitem is not set to 'true'. In this case  no link to open the form will be displayed. 
This attribute can be set by a BusinessRule:


	// lock minutes
	var result={
	         'minuteslocked':true
	}; 

**Note:** locking the minutes does not mean that the minute is not editable at all. It only indicates the the minute body form does not provide 
a link for in-line-editing. The minute can of course be edited by the general workitem UI.  

 
## The MinutePlugin
 
The plugin class  _org.imixs.marty.plugins.minutes.MinutePlugin_ provides additional business logic to handle minutes during 
the processing life-cycle.
A MinuteItem can be of any type (e.g. 'workitem' or 'childworkitem'). The MinutePlugin number all MinuteItems automatically with a continuing
_numSequenceNumber_. The attribute 'minutetype' indicates if a workitem is a minuteparent or a minuteitem.

When a new MinuteItem is created or has no sequencenumber, the plugin computes the next sequencenumber automatically.
In case the minute parent is a version (WORKITEMIDREF), than the plugin copies all MinuteItems from the master and renumbers the MinuteItems
 (sequencenumber). 
 
The Plugin manges the items 'minuteparent' and 'minuteitem'. These items hold a $uniqueID for the corresponding parent
or minute entity. 
  