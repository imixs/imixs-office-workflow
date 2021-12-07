# The Coordinates of a WorkItem

All WorkItems in the marty project are organized in an hierarchical order. A WorkItem is typically assigend to a process and a space entity. These references are stored in the $UniqueIDRef property of the WorkItem. In difference to earlier versions of Marty a WorkItem can have multiple references. This feature is used to weave a WorkItem into a matrix of spaces and processes. So each workitem can hold a reference to a Process and/or to one or more space 
entities. Both reference are optional. But one reference is obligatory.


          Space    Process
              \         /
                Workitem
                /        \
         Workitem     ChildWorkitem


Each Workitem computes structural information from the coordinates where the workitem is located.

## Sub- and Child-WorkItems

A WorkItem can also be organized into a structure of Workitems. In this case the Workitem  holds a reference to another Workitem (ParentWorkitem). Also here the Workitem computes  structural information from its coordinates in the hierarchical structure.

## Structural Information

The following table shows the different properties and information hold by each workitem:


*-------------------+---------------------------------------------------------------+
||	Property		||Infomation													|
*-------------------+---------------------------------------------------------------+
|$UniqueIDRef		| List of references to projects and/or process groups. The reference can also point to another workItem|
*-------------------+---------------------------------------------------------------+
|namProcessTeam		|All team members of a referenced process			|
*-------------------+---------------------------------------------------------------+
|namProcessManager	|All managers of a referenced process			|
*-------------------+---------------------------------------------------------------+
|namProcessAssist	|	All assists of a referenced process			|
*-------------------+---------------------------------------------------------------+
|namSpaceTeam		|All team members of referenced spaces			|
*-------------------+---------------------------------------------------------------+
|namSpaceManager	|	All managers of referenced spaces			|
*-------------------+---------------------------------------------------------------+
|namSpaceAssist		|All assists of referenced spaces			|
*-------------------+---------------------------------------------------------------+
|txtProcessName		|	 Name to a referenced process			|
*-------------------+---------------------------------------------------------------+
|txtProcessRef	 	|Reference to a process			|
*-------------------+---------------------------------------------------------------+
|txtSpaceRef		| Reference to referenced spaces			|
*-------------------+---------------------------------------------------------------+

The properties txtProcessRef and txtSpaceRef are optional and will be computed by the teamPlugin

## The Matrix

To organize the relationship between a Process, Spaces and Workflows and Process Entity  provides a matrix to assign workflows to a process. (in early versions only the project  contained a list of workflows). Each Process can assign mulitple workflows and spaces.

*-----------+-----------+---------------+-------------------+-------------------+-----------+
||			||Auftrag   ||Bestellung	|| Rechnungseingang	|| Rechnungsausgang	|| Mahnung	|
*-----------+-----------*---------------*-------------------*-------------------*-----------*
|Buchhaltung| X    		| X				|					|					|			|		
*-----------+-----------*---------------*-------------------*-------------------*-----------*
|Einkauf	|			|				|		 X			|					|			|
*-----------+-----------*---------------*-------------------*-------------------*-----------*
|Lager		|			|				|					|					|	 X		|
*-----------+-----------*---------------*-------------------*-------------------*-----------*
|Entwicklung|			|				|					|	 X				|			|
*-----------+-----------*---------------*-------------------*-------------------*-----------*


To assign a workitem to a new space or process you can add a the following tags into the 
 Activity result:

	<item name="space">...</item>
	<item name="process">...</item>
   