# Archive & DMS Funtionality

Imixs-Office-Workflow provides you with the functionality of an audit-proof archive. It is essential to distinguish between audit-proofing and archiving. 

## Audit-Proofing 

You ensure audit-proofing yourself through the workflow model. Every workflow model should have at least one final workflow status where no changes (events) can occur. This prevents users from making retrospective changes to data.

<img src="archive/archive-01.png" />

The audit log is generated through the workflow history, which you also define via modeling. Additionally, the workflow system has a technical audit log that cannot be influenced. These data are fixed in the item '$eventlog'.

```xml
<item name="$eventlog">
  <value xsi:type="xs:string">2024-03-21T11:13:27.214|auftrag-1.0.0|7000.20|7010|</value>
  <value xsi:type="xs:string">2024-04-09T16:02:46.815|auftrag-1.0.0|7010.20|7020|</value>
  <value xsi:type="xs:string">2024-04-09T16:03:02.567|auftrag-1.0.0|7020.41|7020|</value>
</item>
```

## Archive

The second aspect is archiving or the physical storage of data. The data in Imixs-Workflow is stored in an SQL database. The data model is flat and is mainly represented by the 'Document' table, which stores all data in a blob field. In addition when using the [Imixs-Archive-API](https://github.com/imixs/imixs-archive/tree/master/imixs-archive-api), so-called 'snapshots' are generated. Snapshots are created by the workflow engine at each process step. A snapshot stores all data (process information + documents) in an immutable record that cannot be manipulated by the workflow engine or the user. You can verify these data yourself via the Imixs Rest API:

https://office.imixs.com/api/documents/a8af44af-964f-4778-b269-4205f22744f3

The last part of the URL is the UniqueID of a document. In the data, you will find the "$snapshotid". This points to the snapshot record with the documents. For example:

https://office.imixs.com/api/documents/a8af44af-964f-4778-b269-4205f22744f3-1715076814056?format=xml

## Search, Backup and Archive Strategies

Documents are stored along with the process data and metadata in the 'Document' table. Essentially, all file formats and types of data can be stored. Imixs-Archive uses the OCR function to scan the contents of a file and allows full-text search. Which files are full-text indexed can be determined via the Tika option 'filepattern': 

```xml
    <tika name="filepattern">(PDF|pdf)$</tika>
```

The search itself allows both full-text search and structured search, i.e., searching with OR, AND, etc., for specific properties. These properties must be defined via the Lucene settings in the configuration file 'imixs.properties' (/src/main/resources/). More information can be found [here](https://www.imixs.org/doc/engine/queries.html).

### Export of files

Using the [Imixs-Archive-Exporter project](https://github.com/imixs/imixs-archive/tree/master/imixs-archive-exporter), individual documents can also be stored (exported) in an external data source. The Imixs-Archive-Exporter runs as an independent container and can therefore also be operated physically separated on another server. The files to be exported are defined via the [Imixs-Event-Log system](https://www.imixs.org/doc/engine/eventlogservice.html). The files are not automatically removed from the process instance. This is initially only about the extraction of files.

### Archive and Backup Strategy

Imixs-Archive offers another system with the [Imixs-Archive Backup Service](https://github.com/imixs/imixs-archive/tree/master/imixs-archive-backup), which enables the backup of process instances in real-time. Here, similar to the Imixs-Archive-Exporter, the data is transferred from an independent container to a file system or an FTP server. The difference here is that the complete snapshot is secured. The snapshot (see URL example above) contains all documents as well as all metadata. The backup system can also be used for data restoration. You should definitely use this backup system to enable disaster recovery.

