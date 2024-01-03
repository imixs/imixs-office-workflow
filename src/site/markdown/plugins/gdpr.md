# The GDPR-Anonymise Service

Imixs-Office-Workflow provide a generic GDPR erasure and anonymise service. This feature can be used to anonymise or delete personal data form any workflow instance. 

To activate this functionallity the Adapter class `org.imixs.workflow.office.gdpr.GDPRAnonymiseAdapter` can be added to any scheduled workflow event defining a GDPR deletion period. This is conform to [Art. 17 GDPR Right to erasure](https://gdpr-info.eu/art-17-gdpr/).

## Configuration

This SignalAdapter `org.imixs.workflow.office.gdpr.GDPRAnonymiseAdapter`  allows the deletion or anonymisation of items.

To trigger the process the following event-item definition is used:

```xml 
<gdpr>
    <delete>...</delete>
    <anonymise>...</anonymise>
    <placeholder>no data</placeholder>
    <references>OUT|IN|ALL|NONE</references>
</gdpr>
```


 - delete = list of items to be deleted
 - anonymise = list of items to be anonymized
 - placeholder = test for anonymization
 - references = recursive search of references

The item definitions `delete` and `anonymise` can contain a comma separated list of items names as also a regular expression. See the following example:


```xml 
<gdpr>
    <delete>$file, cdtr.iban,dbtr.iban,cdtr.bic,dbtr.bic, $workflowsummary, $workflowabstract,txtworkflowhistory</delete-items>
    <anonymise>(^contract\.|^loan\.)</anonymise>
    <placeholder>no data</placeholder>
    <references>ALL</references>
</gdpr>
```

In the example above the items `$file, cdtr.iban,dbtr.iban,cdtr.bic,dbtr.bic, $workflowsummary, $workflowabstract,txtworkflowhistorye`  will be deleted. 
All items starting with `contract.` or `loan.`  will be replaced with the place holder 'no data'.



### References

The tag `references` specifies if the adapter class should also anonymise ingoing or outgoing references. A reference is a workitem linked by the Item `$workitemref`.

 - OUT = all direct refered workitems will be anonymised. These are typical workitems created by the [SplitAndJoinPliugin](https://www.imixs.org/doc/engine/plugins/splitandjoinplugin.html).
 - IN = all workitems holding a reference to the current workitem. These are workitems with a simple unidirectional link to the current workitem in the item `$workitemref`.
 - ALL = all ingoing and all outgoing refrences will be anonymised
 - NONE = no rereferences will be anonymised (default)


### Placeholder Value

The tag 'placeholder' can be used to define a placeholder value for the anoynmization process. If no placeholder is set, the value will be cleared. 

