# Text Adapter

Imixs-Office-Workflow supports various text adapters to be used to adapt static text with processing information during the processing-life cycle. This functionality is based on the [Imixs-Workflow Text Adapter](https://www.imixs.org/doc/engine/adapttext.html) technology. 


## Username Text Adapter

The Username-Text-Adapter provides a mechanism to translate a userid with the corresponding user name. To use this feature, it is sufficient to embed a UserID information in the XML tag `<username>....<username>` . See the following example:
 

    Request was created by <username>$creator</username>.

This will replace the value of the item $creator with the corresponding user display name. 
 
You can use this text adapter in various situations like E-Mail templates, History entries or business rules.



## The Country Code Adapter

The CountryCodeAdapter can be used to replace  country codes with the international country name. See the following example:


	The company is based in<countryname>company.countrycode</countryname>}
   
This will replace a `company.countrycode = DE` into _Germany_. 

Optional a locale can be provided to specify the target language. The following example with translate the country code 'DE' into 'Deutschland'. 
	
	<countryname locale="de_DE">company.country</countryname>
    

## The WorkitemRef Adapter

WorkitemRefAdapter replaces item values embedded into the tag `<workitemref>..</workitemref>` with the item value from the corresponding workitem. The the following example:


	Plant ID : <workitemref>plant.id</workitemref>

This example will replace the tag with the plant ID of a referred workitem. See also the section  [Workitem Linking](../fomrs/workitemlinking.html) for more information how to link workitems. 

A Workitem can hold multiple referenced to different sub processes. You can use an optional filter attriubte to define search filter for a specific linked process instance. The following example will refere to the subprocess with the workflowgroup `Plant`. 

 	Plant ID : <workitemref filter="($workflowgroup:Plant)">plant.id</workitemref>.
 