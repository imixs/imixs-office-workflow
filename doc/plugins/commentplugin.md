# The CommentPlugin

The marty CommentPlugin provides functionality to store a comment history.

    org.imixs.marty.plugins.CommentPlugin
 
## Comments

The marty ApplicatioPlugin provides an extended comment feature. 

Comments entered by a user into the field 'txtComment' are stored in the list property 'txtCommentList' which contains a map for each comment entry. 
The map stores the following attributes:

* nameditor - current username
* datcomment - timestamp when the comment was added
* txtcomment - the comment

The last entered comment will be stored into the property 'txtLastComment' which can be used for additional processing instructions (e.g. a E-Mail message text)

## How to model a Comment
The comments added into the txtCommentList can be also controlled by the corresponding workflow event by adding a result instruction:

### Ignore Comments

    <comment ignore="true" /> 
    
This tag ignores a new comment which will not be added into the comment history. The property 'txtComment' of the workitem will not be cleared. This feature can be used to store comments when only a update event is triggered, but no comment should be added into the comment list.
 
 
### Fixed Comments

    <comment>xxx</comment> 
    
This tag adds a fixed comment 'xxx' into the comment list. The property 'txtComment' will be ignored but cleared. 



 