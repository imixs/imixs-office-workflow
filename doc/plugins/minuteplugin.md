# The MinutePlugin

The plugin class _org.imixs.marty.plugins.minutes.MinutePlugin_  controls MinuteItems of a parent workflow.

A MinuteItem can be of any type (e.g. 'workitem' or 'childworkitem'). The plugin computes minute-numbers for all MinuteItems automatically with a continuing numSequenceNumber. The attribute 'minutetype' indicates if a workitem is a minuteparent or a minuteitem.

When a new MinuteItem is created or has no sequencenumber, the plugin computes the next sequencenumber automatically.

In case the minute parent is a version (WORKITEMIDREF), than the plugin copies all MinuteItems from the master and renumbers the MinuteItems (sequencenumber). 

The Plugin manages the items 'minuteparent' and 'minuteitem'. These items hold a $uniqueID for the corresponding parent
 or minute entity. 


See also the [MinuteController](../controller/minutecontroller.html)

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


## Auto-Updates for for Minute Items.

In case a MinuteItem is processed, the _MinutePlugin_ automatically refreshes the header Fields:

 * \_team 
 * \_present    

The values are copied from the parent workitem.


# Reset Version History
In some cases it is useful to delete the version history of a minute. This is the case when working with templates. A minute created from a template is not necessarily a version of the template. In such a case the common version identifier '_$workitemID_' can be deleted by setting the event item 'resetminuteversionhistory' to the boolean 'true'.


	<item name="resetminuteversionhistory" type="boolean">true</item> 