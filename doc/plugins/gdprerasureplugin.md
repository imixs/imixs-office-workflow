# The GDPR-ErasurePlugin

The plugin class 'org.imixs.marty.plugins.GDPRErasurePlugin' allows the deletion or anonymisation of items within a running
 workflow process. This is conform to [Art. 17 GDPR Right to erasure](https://gdpr-info.eu/art-17-gdpr/).
  
  
To trigger the process the following event-item definition is used:

	<item name="gdpr-erasure">
		<delete>$file</items>
		<anonymize>firstname,lastname</items>
		<placeholder>no data</placeholder>
	</item>

The item name list for anonymization and deletion must be comma separated. 
In the example above the items 'fristname' and 'lastname' will be anonimized and the item '$file' will be deleted.

The item list can be customized for each event separately


## Placeholder Value

The item 'placeholder' can be used to define a placeholder value for the anoynmization process. If no placeholder is set, the value will be cleared. 

