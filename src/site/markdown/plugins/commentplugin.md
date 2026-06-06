# The CommentPlugin

The marty CommentPlugin provides functionality to store a comment history.

    org.imixs.marty.plugins.CommentPlugin

## Comments

The Plugin provides an extended comment feature.

Comments entered by a user into the field `comment.user` are stored in the list property `comment.log` which contains a map for each comment entry.
The map stores the following attributes:

- user - current username
- date - timestamp when the comment was added
- message - the comment

The last entered comment will be stored into the property 'txtLastComment' which can be used for additional processing instructions (e.g. a E-Mail message text)

## How to model a Comment

The comments added into the comment.log can be also controlled by the corresponding workflow event by adding a result instruction:

### Ignore Comments

```xml
<comment>
 <ignore>true</ignore>
</comment>
```

This tag ignores a new comment which will not be added into the comment history. The property `comment.user` of the workitem will not be cleared. This feature can be used to store comments when only a update event is triggered, but no comment should be added into the `comment.log`.

### Fixed Comments

```
<comment>
 <message>Hello World</message>
</comment>
```

This tag adds a fixed comment 'Hello World' into the comment list. The workitem field `comment.user` is still fully supported
