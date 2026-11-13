# Lucene Search Feature

Imixs-Office-Workflow uses the standard Lucene Search feature provided by Imixs-Workflow. See details [here](https://www.imixs.org/doc/engine/queries.html).

The configuration can be extended by the following imixs.properties entries:

```
index.directory=${imixs-office.IndexDir}
index.fields=txtsearchstring,_projectnumber,_projectname,_ordernumber,_contractnumber
index.fields.analyze=_projectnumber
index.fields.noanalyze=ProcessID,projectDate
index.fields.store=process.name,projectDate
```

## office.search.noanalyze

If you want to search item values in special fields marked in the property 'index.fields.noanalyze' then you can add the additional property

    office.search.noanalyze=order.number

This allows you to search for an search phrase containing special characters like :

    + - && || ! ( ) { } [ ] ^ " ~ * ? : \ /

For example if you add 'order.number' into the property 'index.fields.noanalyze' and the property 'office.search.noanalyze' than you can search for:

    1314/03/FV/LIG/2021/EU

Searching for this search phrase without this feature shows no results!
