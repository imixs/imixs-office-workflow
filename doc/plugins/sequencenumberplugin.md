# The SequenceNumberPlugin

The plugin class _org.imixs.marty.plugins.SequenceNumberPlugin_ automatically updates the sequence-number for a workitem during the processing phase. See the [SequenceService](../services/sequencenumberservice.html) for details.
  
  
## SequenceService

The EJB _org.imixs.marty.ej.SequenceService_ provides a method to compute a sequence number for a workitem. The service is used 
by the [SequeceNumberPlugin](../plugins/squencenumberplugin.html).

Sequence numbers are defined by the BASIC configuration document in the item "_sequencenumbers_". This item holds a list of sequence numbers for each workflow group:

	Invoice Request=100001
	Budget Request=200002
	[GENERAL]=9000001	

In case a workitem with the workflow group "Invoice Request" is processed, the next sequence number 100001 will be choosen and the next sequence number will be increased to  100002.


### General Sequencenumber  

In case the identifier _[GENERAL]_ is used, a sequence number will be computed even if the workflow group is not defined by the configuration document. This feature can be used to give a unique number over all workitems independent from their workflow group. 
  