# Workflow Models

Each workflow model provided in Imixs-Office-Workflow need to be defined by a unique 
Model-Version Number.

The model Version is expected in the format:

	DOMAIN-LANGUAGE-VERSIONNUMBER

e.g.

	office-de-0.0.1

If a Modelversion did not contains at least 3 tokens the model can not be assigned to a process.

## Workflow Plug-Ins
Imixs Office Workflow delegates a lot of the business logic into Workflow Plugins. 
 For that reason a Workflow Model should at least contain the following plugins:

 * org.imixs.workflow.plugins.RulePlugin

 * org.imixs.marty.plugins.ProfilePlugin

 * org.imixs.marty.plugins.SequenceNumberPlugin

 * org.imixs.marty.plugins.TeamPlugin

 * org.imixs.workflow.plugins.AccessPlugin

 * org.imixs.workflow.plugins.OwnerPlugin

 * org.imixs.workflow.plugins.jee.HistoryPlugin

 * org.imixs.workflow.plugins.LogPlugin

 * org.imixs.marty.plugins.ApplicationPlugin

 * org.imixs.workflow.plugins.ResultPlugin

 * org.imixs.marty.plugins.MailPlugin

 * org.imixs.workflow.plugins.jee.extended.LucenePlugin


**The order should not be changed!**


# The System Models

The Imixs-Office-Workflow provides a system model which is responsible for the system entities

* Profile
* Space
* Process

The model version is:

	system-de-1.0.0

	
	
### Profile
The Profile workflow Model can be used to set a profile active or inactive

	200 - 209 = Profile inactive
	210 - 249 = Profile active
	