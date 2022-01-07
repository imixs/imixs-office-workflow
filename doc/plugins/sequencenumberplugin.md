# The SequenceNumberPlugin

The plugin class _org.imixs.marty.plugins.SequenceNumberPlugin_ automatically updates the sequence-number for a workitem during the processing phase. See the [SequenceService](../services/sequencenumberservice.html) for details.
  
  
## SequenceService

The EJB `org.imixs.marty.ej.SequenceService` provides a method to compute a sequence number for a workitem. The service is used by the [SequeceNumberPlugin](../plugins/squencenumberplugin.html). The computed sequence numer is stored into the item `sequencenumber`. 

Sequence numbers are defined by the BASIC configuration document in the item `sequencenumbers`. This item holds a list of sequence numbers for each workflow group:

	Invoice Request=100001
	Budget Request=200002
	[GENERAL]=9000001	

In case a workitem with the workflow group "Invoice Request" is processed, the next sequence number 100001 will be chosen and the next sequence number will be increased to  100002.


### General Sequencenumber  

In case the identifier _[GENERAL]_ is used, a sequence number will be computed even if the workflow group is not defined by the configuration document. This feature can be used to give a unique number over all workitems independent from their workflow group. 


### Number Formating

A sequence number can also be defined with a fixed length of leading 0. For example:

	Budget Request=000002

The sequence Service will than generate a String representation of the next number

In addition also leading characters are supported:

	Budget Request=R-000002
	Budget Request=R000002


### Date Formating

By placing the tag `<date>` in the sequence number, the current date in a custom format can be added into the sequence number:

	Budget Request=R-<date>YYYY</date>-000002

will result to

	Budget Request=R-2022-000002
  



