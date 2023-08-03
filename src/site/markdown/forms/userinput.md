# User Input & Selection Parts 

Imixs-Office-Workflow provides a set of custom input parts to enter usernames or select usernames from a organization unit. 

# The User Input

The item part `userinput` can be used to edit a single user name. The part provides a lookup feature for profile names

```xml
    <item name="user"
          type="custom"
          path="userinput"
          label="User:" />
```

## The User-List Input

Optional the User-List-Input allows to enter a list of user names. The part provides a lookup feature for profile names

```xml
    <item name="userlist"
          type="custom"
          path="userinput"
          label="User:" />
```


### User Input by Space

 The custom part `userinputbyspace` can be used to display a Combobox with usernames from a space. The space and the member list can be defined by the 'options'

 Example:

```xml
   <item name="audit.auditor" required="true"
      type="custom"  
      path="userinputbyspace"
      options="Auditoren;space.manager"
      label="Auditor:" />
```

