# The DeputyPlugin

The DeputyPlugin updates the name fields 
(prafix = 'nam') with the corresponding deputy information from a user profile.

	org.imixs.marty.plugins.DeputyPlugin
  
If a user has a deputy entry in the corresponding user profile entity this user entry will be added to the name field during a process step.  
In case a namField is listed in the 'ignoreList' or the name ends with 'approvers' or 'approvedby' the field will be skipped.
  
The plug-in runs on all kinds of workitems and childworkitems. The plug-in should run after the TeamPlugin but before the ownerPlugin,
 accessPlugin and ApproverPlugin
 