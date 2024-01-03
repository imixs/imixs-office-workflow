# The GDPR-Anonymise Service

Imixs-Office-Workflow provide a generic GDPR erasure and anonymise service. This feature can be used to anonymise or delete personal data form any workflow instance. 

To activate this functionallity the Adapter class `org.imixs.workflow.office.gdpr.GDPRAnonymiseAdapter` can be added to any scheduled workflow event defining a GDPR deletion period. This is conform to [Art. 17 GDPR Right to erasure](https://gdpr-info.eu/art-17-gdpr/).

## Configuration

This SignalAdapter `org.imixs.workflow.office.gdpr.GDPRAnonymiseAdapter`  allows the deletion or anonymisation of items.

To trigger the process the following event-item definition is used:

```xml 
<gdpr>
    <delete-items>...</delete-items>
    <anonymise-items>...</anonymise-items>
    <placeholder>no data</placeholder>
    <anonymise-workitemref>true|false</anonymise-workitemref>
</gdpr>
```


 - delete-items = list of items to be deleted
 - anonymize-items = list of items to be anonymized
 - placeholder = test for anonymization
 - anonymise-workitemref = recursive search of references

The item definition can contain a comma separated list of items as also a regular expression. See the following example:


```xml 
<gdpr>
    <delete-items>$file, cdtr.iban,dbtr.iban,cdtr.bic,dbtr.bic, $workflowsummary, $workflowabstract,txtworkflowhistory</delete-items>
    <anonymise-items>(^contract\.|^loan\.)</anonymise-items>
    <placeholder>no data</placeholder>
    <anonymise-workitemref>true</anonymise-workitemref>
</gdpr>
```

In the example above the items '$file, cdtr.iban,dbtr.iban,cdtr.bic,dbtr.bic, $workflowsummary, $workflowabstract,txtworkflowhistorye' will be deleted. 

All items starting with 'contract.' or 'loan.'  will be replaced with the place holder 'no data'.

The item list can be customized for each event separately


### Placeholder Value

The item 'placeholder' can be used to define a placeholder value for the anoynmization process. If no placeholder is set, the value will be cleared. 

