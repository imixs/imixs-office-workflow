# Form Expressions

It is possible to hide a input part based on a EL Expression providing the attribute `hide`

```xml
<item name="myItem" type="selectOneRadio" required="true"  label="Budget"
        hide="#{!myController.isFinanceTeam()}"
        options="yes|Yes;no|No"/>
  </imixs-form-section>

```

If the attribute expression validates to true, the item will be hidden.

Valid expressions must be defined during deployment time in the resource file `customform.expressions`

```
├── resources
│   ├── ...
│   ├── customform.expressions
```

See the following example:

```
######################################################################
# Contains a list of supported EL Expressions used in Models
######################################################################

#{!teamController.isManagerOf(workflowController.workitem.item['process.ref'])}
#{!teamController.isManagerOf(workflowController.workitem.item['space.ref'])}
#{!teamController.isTeamMemberOf(workflowController.workitem.item['process.ref'])}
#{!teamController.isTeamMemberOf(workflowController.workitem.item['space.ref'])}
#{!teamController.isAssistOf(workflowController.workitem.item['process.ref'])}
#{!teamController.isAssistOf(workflowController.workitem.item['space.ref'])}
#{!teamController.isMemberOf(workflowController.workitem.item['process.ref'])}
#{!teamController.isMemberOf(workflowController.workitem.item['space.ref'])}

# CUSTOM Expressions:

#{!myController.isFinanceTeam()}
```
