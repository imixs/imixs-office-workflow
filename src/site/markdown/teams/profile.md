# User Profile Management 

Imixs-Marty provides a user profile management. A User Profile is automatically created after a user logged in the first time. 
The creation of a new profile is triggered by the userController CDI Bean in the @PostConstruct method. The creation is managed by the EJB *ProfileService*. The User Profiles are stored in the document database with the type attribute *'profile'*.

Additionally the Imixs-Marty project provides a [User-Database](userdb.html). If the User-Database is enabled, the userid and password can be stored into a local user table. In this case new user profiles can also be created manually by the administrator where the UserID and a Password is provided along the creation process. 


### Autoprocess a New User Profile
When a user profile is created the first time, the profile will be automatically be processed by the Imixs-Workflow engine with the *ProcessID=200* and the *ActivityID=5*. 
The System Model files *system-de.bpmn* and *system-en.bpmn* provide a valid BPMN model. 


### Autoprocess a User Profile During the Login
In addition the user profile can be processed and updated by the Imixs-Workflow engine each time the user login to the system. 
To enable this mode,  the system property *'profile.login.event'* can be set with a valid BPMN Event ID:

	profile.login.event=10

Optional this variable can also be set as an environment variabel: 

    PROFILE_LOGIN_EVENT: "10"

**Note:** Autoprocessing a user profile during login can also be combined with other login adapters. E.g. if a LDAP Profile setup exists, the attributes of an LDAP entry will be automatically synchronized during the login process. 


## The ProfilePlugin

The Plugin org.imixs.marty.plugins.ProfilePlugin provides additional business logic for profile entities and workitems. The plugin is called by the System Workflow Model. When a userProfile is  processed (typically when a User logged in) the plugin validates the username and email address.


### UserID validation

The userID is automatically validated for uniqueness. It is not possible to store two profiles with the same userid (txtName). In addtion the userid is validated against a Input Pattern. The default pattern is:

    ^[A-Za-z0-9.@\\-\\w]+
    
The pattern can be changed by the imixs.property 

    security.userid.input.pattern=
    
With the imixs.property '_security.userid.input.mode_' the userID can be forced to lower or upper case

    security.userid.input.mode=
    
Possible values are: '_LOWERCASE_', '_UPPERCASE_' and '_NONE_'

### Email Validation

In the default configuration a E-Mail attribute is mandatory for a profile. The Profile validates the email also for uniqueness. In this cas it is not possible to store two profiles with the same email (txtEmail).

To deactivate the e-mail validation the system property '_security.email.unique_' can be set to true:

    # force unique email addresses
    security.email.unique=true

To deactivate the validation for unique email the property can set to 'false'. 

	# allow duplicated email addresses
    security.email.unique=false

## How to Translate userIDs into a User Name

The Marty project provides a mechanism to translate userIDs in a BPMN Event or BPMN Task with the corresponding User name stored in the User Profile
This mechanism is controlled by the TextUsernameAdapter.
See the following example where a BPMN Event text (e.g. History text or Email Body) 

	Request saved by <username>$editor</username>.

See the section [TextUsernameAdapter](../textadapter/index.html) for more details. 


