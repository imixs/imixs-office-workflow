# Mailing

Imixs Office Workflow supports a mail interface which builds up on the Jakarta EE JNDI Resources.
To enable mailing a mail resource need to be added into the application server. Check your Jakarta EE Application server for details how to add a mail resource. The default container from Imixs-Office-Worklfow already provides a configuration out of the box.

## The Mail Plugin

The Marty Mail Plugin extends the functionality of the Imixs JEE Mail Plugin. The Plugin translates user names into the mail addresses configured in the user profiles.

### Attachments

Through the mail configuration in the Imixs Modeler it is possible to attach files which are part of a workitem (BlobWorkitem). To attach all files to a mail the following mail text can be added into the mail body

     <attachments></attachments>

If a specific attachment should be attached you can name the attachment by the filename

     <attachments>order.pdf</attachments>

In this case only the attachment 'oder.pdf' will be attached to the email.

### Regular Expressions

It is also possible to define a Attachment name based on a regulare expression.

     <attachments>order.pdf</attachments>

For example see the following expression to validate file formats for .gif or .jpg

     <attachments>([^\s]+(\.(?i)(jpg|png|gif|bmp))$)</attachments>

## Configuration

Additional mail configuration can be set in the Imixs Properties according to the Java Mail specification. In addition also a custom subclass of the MailPlugin can change the behavior by overwriting specific methods. Find details in the [Imixs-Workflow main project](https://www.imixs.org/doc/engine/plugins/mailplugin.html).

### Default Sender Address

The default sender address will be set with the current user name. The sender address can be changed by the imixs property ‘mail.defaultSender’. If the property is defined, the plugin overwrites the ‘From’ attribute of every mail with the DefaultSender address. If no value is set the mail will be send from the current users mail address.

Example (imixs.properties):

```
# Mail Plugin
mail.defaultSender=info@foo.com
```

### ReplyTo Address

You can set a explizit replyTo address by setting the imixs.property mail.replyTo.

```
# Mail Plugin
mail.replyTo=marketing@foo.com
```

### Authenticated Sender

You can overwrite the authenticatedSender which may be necessary in case of DMARC (Domain-based Message Authentication, Reporting, and Conformance).

DMARC checks to see if the email's sender address is actually authorized to send emails for that domain. A conflict can occur if:

1. From address: info@foo.com (customer domain)
2. Actual sender: webmaster@my-company.com (your domain)

For DMARC authentication to be successful, at least two conditions must be met:

1. SPF (Sender Policy Framework): The server from which the email is sent must be authorized in the SPF records of the sender domain.
2. DKIM (DomainKeys Identified Mail): The email must be signed with a private key of the sender domain.

You can set the authenticated sender in Imixs-Workflow by setting the property `mail.authenticatedSender` to the same address as the configured mail host.

```
# Authenticated Sender Address
mail.authenticatedSender=marketing@foo.com
```

### Testing Mode

During development you can switch the Imixs-MailPlugin into a Testing-Mode by defining the imixs property `mail.testRecipients`. The property value can contain one ore many (comma separated) Email addresses. If the property is defined, than an e-mail message will be send only to those recipients. The Subject will be prefixed with the text 'TEST: '.

Example (imixs.properties):

```
#Testmode
mail.testRecipients=test@development.com
```

### CharSet

The default character-set used for the mail subject and body parts is set to ‘ISO-8859-1’. It is possible to switch to a specific character set . There for the imixs.property key ‘mail.charSet’ can be used.

Example (imixs.properties):

```
#Charset
mail.charSet=UTF-8
```

mail.authenticatedSender=Alexander Global Logistics - {username} <webmaster@imixs.com>

### Microsoft Exchange Server

If outgoing mails are send to an MS Exchange server the following additional properties should be set:

    mail-smtp.quitwait = false
    mail-smtp.sendpartial = true
