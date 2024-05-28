# Organization Units

Imixs-Office-Worklfow organizes the hierarchical order of a workitem between
processes, spaces and workitems and computes the users associated with an orgunit.  
 
A WorkItem is typically assigned to one or more orgunits. These references are stored in the item `$UniqueIDRef`. 
TeamPlugin automatically computes the references and stores the information into the items 
`process.ref` and `space.ref` which containing only uniqueIDs of the corresponding orgunit type.

The items `process.ref` and `space.ref` can also be modified by the workflow model or a custom business logic.
 
The Marty TeamPlugin computes additional workflow properties:

  
| Item       		| Type      | Description                               						|
|-------------------|-----------|-------------------------------------------------------------------|
|space.team   		| names		| current team members of an associated space orgunit. 				|
|space.manager		| names   	|current managers of an associated space orgunit.					|
|space.assist		| names   	|current assists of an associated space orgunit. 					|
|space.name			| text		|name of  an associated space orgunit. 								| 
|space.rref			| text		|$uniqueID  of an associated space orgunit. 						| 
|process.team		| names		|current team members of an associated process orgunit. 			| 
|process.manager	| names		|current managers of an associated process orgunit. 				| 
|process.assist		| names		|current assists of an associated process orgunit. 					| 
|process.name		| text		|name of  an associated process orgunit. 							| 
|process.ref		| text		|$uniqueID  of an associated process orgunit.						| 
 
The name items can be used in ACL settings or mail settings.
 
The item `process.ref`  and `space.ref` are optional and can update the current $uniqueIDs for referenced orgunits. 
The Plug-in updates the item `$UniqueIDRef` automatically if these properties are filled.

## Archived Spaces

Spaces can optional be archived. Archived spaces can be still managed by the orgunit owner. 


<img class="screenshot" src="teamplugin-model.png" />


The teamlist of an archived space is still updated into the workitem. But the [Team Interceptor](./teaminterceptor.md) will ignore archived spaces.  


# The Team Plugin

The relationship of teams assigned to a workitem is automatically managed by the *TeamPlugin*. 

     org.imixs.marty.team.TeamPlugin

You should ensure that this plugin is added to every business workflow model. 

## Evaluate a Orgunit

If the workflow result message of an Imixs-Event contains a space or process reference the plug-in will update the references

Example:

	<item name="space">...</item>
	<item name="process">...</item>


# The SpacePlugin

The SpacePlugin is a system plugin running only in the system workflow groups 'space' and 'process'.

The plugin computes and updates the attributes of a space with in a hierarchical order.

    org.imixs.marty.team.SpacePlugin

The hierarchical order of a space is defined by the property `$uniqueidref` which is optional and pointing to a parent space entity. 
The plugin updates the following properties of a space entity:


 * name = combined name of the parent space name and the own name separated by a '.'
 * space.parent.name = name of the parent space in case the space entity is a subspace
 

## Unique Name

A process or a space has a unique name attribute `name`. If the name provided by the user is already taken the plugin throws a PluginException. 

## Archived Spaces

Spaces can optional be archived. Archived spaces can be still managed by the orgunit owner. 
 
 
 
# The Team Interceptor

The EJB Interceptor class *org.imixs.marty.ejb.TeamIntercepter* provides a mechanism to compute the orgunits a user belongs to. An orgunit can either be a 'Process' or a 'Space'. The Result is put into the EJB contextData which is
 read by the [DocumentService](http://www.imixs.org/doc/engine/documentservice.html) to grant access by dynamic computed user roles.

An orgunit in the Imixs-Marty project contains 3 different roles 

* Manager - responsible for a single orgunit
* Team - team members assigned to an orgunit
* assist - optional list of users to assist this orguinit

The syntax for the user roles computed by the TeamInterceptor is :

    {ORGUNIT:NAME:ROLE}

For example a user with the role 'assist' in the process named 'Finance' is computed as:

    {process:Finance:assist}
 
### Generic Roles 
 
If a user is at least member of one of the roles associated with an orgunit, the generic orgunit role '*member*' is added: 

    {process:Finance:member}

In addition generic roles are computed independent from a specific orgunit. So as a result it is also possible to ask, if the user is member of a role associated with an orgunit independent form the orgunit itself. In this case the name of the orgunit is skipped. 

Back to the example above, if a user is assigned with the role 'assist' in the process named 'Finance' the complete list of role names computed by the _TeamInterceptor_ is:

    {process:Finance:assist}
    {process:Finance:member}
    {process:member}
    {process:assist}

### Roles declared by Name or UniqueID

A role generated by the _TeamInterceptor_ are computed by the name of a orgunit as also by the _$uniqueid_ of the orgunit:

    {process:Finance:assist}
    {process:8838786e-6fda-4e0d-a76c-5ac3e0b04071:assist}



## Modeling

The custom orgunit role names can be used in a BPMN 2.0 model.  

<img class="screenshot" src="acl001.png"/>


In this example the read access for a task is extended to the orgunit role '{process:Finance:assist}'.

In combination with the [TeamRoleWildcardAdapter](./teamrolewildcardadapter.md) the orgunit role can be also computed automatically. There for the wildcard '?' can be used. 

<img class="screenshot" src="acl002.png"/>


The associated orgunit will be computed by the TeamRoleWildcardAdapter.




## Configuration
    
The interceptor can be enabled by the deployment descriptor of the *DocumentService*. See the following example for a ejb-jar.xml configuration

    <assembly-descriptor>
		<!-- TeamInterceptor -->
		<interceptor-binding> 
		    <description>Intercepter to add orgunit-role mapping into EJB Context Data</description> 
		    <ejb-name>DocumentService</ejb-name> 
			<interceptor-class>org.imixs.marty.ejb.TeamInterceptor</interceptor-class> 
		</interceptor-binding>
	</assembly-descriptor>
  
 
 
 
 
# The TeamRoleWildcardAdapter

The `TeamRoleWildcardAdapter` can be used to compute team roles using the '?' wildcard.
 
In combination with the [TeamInterceptor](./teaminterceptor.md) the `TeamRoleWildcardAdapter` computes a Orgunit Roles associated with the current workitem. To add the team role for the orgunit currently associated with the workitem into the ACL the following role definition can be added:

	{process:?:team}
	
<img class="screenshot"  src="acl002.png" />

The _TeamRoleWildcardAdapter_ will lookup the associated process orgunit and compute the role name (e.g. from the orgunit 'Finance'):

    {process:8838786e-6fda-4e0d-a76c-5ac3e0b04071:team}

**Note:** The `TeamRoleWildcar