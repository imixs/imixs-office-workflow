
## Error and Exception Handling 

Imixs Marty provides a ErrorHandler which is called by the WrokflwoContorller to handle and display 
 PluginExceptions an ValidationExceptions. 
 A PluginException is typically thrown by a plugin during the workflow processing.
 A ValidationException can be thrown by an JSF managed bean or CDI bean. This kind of exceptions 
 are handle by evaluating the ObserverException which is thrown during a JSF lifecycle phase. 
 
A ValidationException can provide a list of optional params which will be added into  the JSF Exception to be displayed in the web frontend.
 
The exceptions are automatically handled by the WorkflowController and displayed  by the error.xhmtl page.
 
A ValidationException can be used in any jsf lifecylce. 
 
Example:
 
	 // some code going wrong.....
	   Object[] params={documentContext.getItemValueString("txtName")};
	         throw new ValidationException(MyBean.class.getSimpleName(),
	               ERROR_ATTACHMENTS_MISSING,  "Please enter a valid name",params);
                                       

                           