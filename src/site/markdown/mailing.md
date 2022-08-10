# Mailing 

Imixs Office Workflow supports a mail interface which builds up on the Jakarta EE JNDI Resources. 
 To enable mailing a mail resource need to be added into the application server.

In GlassFish server a new mail resource can be configured from the menu "Resources->Java Mail sessions". 
 The JNDI Name need to be set to: "mail/org.imixs.workflow.jee.mailsession"

See the following example configuration:

	JNDI Name = mail/org.imixs.workflow.mail
	Mail Host = my-mail-host.local
	Default User = Glassfish
	Default Sender Address = Glassfish


Where 'my-mail-host.local' is the dns name or IP address of the mail server.

If the mail server requires an authentication (which is typical for most installations) the following  additional properties need to be added into the section "Additional Properties":

	mail-smtp.auth = true
	mail-smtp.password = glassfish
	mail-smtp.host = my-mail-host.local
	mail-smtp.user = Glassfish

The valid user and password settings should be provided by the mailserver administrator.

## Microsoft Exchange Server

If outgoing mails are send to an MS Exchange server the following additional properties should be set:

	mail-smtp.quitwait = false
	mail-smtp.sendpartial = true


## The Mail Plugin

The Marty Mail Plugin extends the functionality of the Imixs JEE Mail Plugin. The Plugin translates  user names into the mail addresses configured in the user profiles.

### Attachments

Through the mail configuration in the Imixs Modeler it is possible to attach files which are part of a  workitem (BlobWorkitem). To attach all files to a mail the following mail text can be added into the mail body

	 <attachments></attachments>

If a specific attachment should be attached you can name the attachment by the filename

	 <attachments>order.pdf</attachments>
 
 In this case only the attachment 'oder.pdf' will be attached to the email.
 
### Regular Expressions

It is also possible to define a Attachment name based on a regulare expression. 
 
	 <attachments>order.pdf</attachments>

For example see the following expression to validate file formats for .gif or .jpg

	 <attachments>([^\s]+(\.(?i)(jpg|png|gif|bmp))$)</attachments>
 
