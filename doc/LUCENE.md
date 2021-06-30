# Lucene Search Feature

Imixs-Office-Workflow uses the standard Lucene Search feature provided by Imixs-Workflow. See details [here](https://www.imixs.org/doc/engine/queries.html).

The configuration can be done by the following imixs.properties entries:


	lucence.indexDir=${imixs-office.IndexDir}
	index.fields=txtsearchstring,txtSubject,txtname,txtEmail,txtUserName,namCreator,txtworkflowgroup,txtworkflowstatus,txtWorkflowAbstract,txtWorkflowSummary,txtworkflowhistory,txtspacename,txtprocessname,_subject,_description,_name,_projectnumber,_projectname,_ordernumber,_contractnumber,datDueDate,txtcommentlog,htmldescription,htmldocumentation,_childitems
	index.fields.analyze=txtUsername
	index.fields.noanalyze=type,$UniqueIDRef,$created,$modified,$ModelVersion,namCreator,$ProcessID,datDate,txtWorkflowGroup,txtemail, datdate, datfrom, datto, numsequencenumber
	index.fields.store=process.name,txtProcessName,txtWorkflowImageURL

	
	
## office.search.noanalyze	

If you want to search item values in special fields marked in the property 'index.fields.noanalyze' then you can add the additional property

	office.search.noanalyze=order.number
	
This allows you to search for an search phrase containing special characters like :

	+ - && || ! ( ) { } [ ] ^ " ~ * ? : \ /
	
For example if you add 'order.number' into the property 'index.fields.noanalyze' and the property 'office.search.noanalyze' than you can search for:

	1314/03/FV/LIG/2021/EU
	
Searching for this search phrase without this feature shows no results!