# Country Codes

The Imixs-Office-Worklfow provides the CDI bean `CountryController`. providing methods to display a selection of all countries and country codes. The component also includes a text adapter feature to convert a country code into a country display name.

## Country Code Input

To display a selection of a country you can use the custom input part `country`:

```xml
    <item name="contract.country" type="custom" path="country"
	      required="true"  label="Country:" />
```

<img class="screenshot" src="item-country.png" />

## Country Name Text Adapter

The country component typically stores only the ISO country code in a workitem (2 letters).
To display the country name you can use the integrated text adapter feature:

    <countryname locale="de_DE">company.country</countryname>

## The CountryController CDI Bean

The cdi bean `countryController` provides a Java Locale util class to compute all countries. You can use this bean also for custom implementations of country codes. See the following example how to integrate a select box with all countries in JSF you can use the following example:

```xml
<h:selectOneMenu required="#{required}" value="#{workitem.item['company.country']}">
	<f:selectItem itemLabel=""></f:selectItem>
	<f:selectItems value="#{countryController.getCountriesSelectItems()}"></f:selectItems>
</h:selectOneMenu>
```
