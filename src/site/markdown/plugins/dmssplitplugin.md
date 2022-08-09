#The DMSSplitPlugin

The DMSSplitPlugin provides functionality to create sub-process instances for
 each attachment added in an origin process. The configuration is similar to
  the Imixs-Workflow SplitAndJoinPlugin. 
  
    org.imixs.marty.plugins.DMSSplitPlugin
  
 To trigger the creation
 of subprocesses, the item named "dms_subprocess_create" can be added into the workflow result of an BPMN Event.
 
	<item name="dms_subprocess_create">
	    <modelversion>1.0.0</modelversion>
	    <processid>100</processid>
	    <activityid>10</activityid>
	    <items>namTeam</items>
	</item>
 
 The tag 'remove'  indicates that the attachments will be removed from the origin process after
 the subprocess was created.
 
	<remove>true</remove>
 	 
 A subprocess will contain the $UniqueID of the origin process stored in the
  property $uniqueidRef. The origin process will contain a link to the
 subprocess stored in the property txtworkitemRef. So both workitems are
  linked together.
  
The list of attachments will be taken from the BlobWorkitem if it exists. Otherwise the files will be read from the origin workitem. 
 
 