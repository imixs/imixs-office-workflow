# Country Codes

The Imixs-Office-Worklfow provides the CDI bean 'CountryController'. This bean contains methods to display a selection of all countries and also includes a text adater feature to convert a country code into a country display name.
The bean uses the Java Locale util class to compute all countries.

## How to Integrate

To integrate a select box with all countries in JSF you can use the following example:

	<h:selectOneMenu required="#{required}" value="#{workitem.item['company.country']}">
		<f:selectItem itemLabel=""></f:selectItem>
		<f:selectItems value="#{countryController.getCountriesSelectItems()}"></f:selectItems>
	</h:selectOneMenu>

You can also use the form part *pages/workitems/parts/country.xhtml* located in the JSF component

	 <item name="contract.country" type="custom"  path="country" required="true"   label="Membership Country:" />
	 
	
	

## Country Name Text adapter

The country component typically stores only the ISO country code in a workitem (2 letters).
To display the country name you can used the integrated text adapter feature:

	<countryname locale="de_DE">company.country</countryname>

	