# Analytic Parts

'Analytic Parts' can be used to build dashboards and display any kind of analytic data from a business process. The analytic parts are controlled by the CDI bean `AnalyticController`. This bean supports a CDI observer pattern that allows an custom application to provide any kind of individual analytic data.

Imixs-Office-Workflow provides several standard part templates to display numbers, text, charts. An application can adapt this mechanism and provide custom analytic parts.

## How to Integrate

A custom part is added to a form like all other form part. It has a key item and a label:

```xml
  <imixs-form-section columns="3" label="Analytic Data">
	<item name="sales.tax.totals" type="custom" path="cards/plain" label="Tax:" />
    <item name="sales.order.requests" type="custom" path="cards/chart" label="Orders:" options="{...}"/>
  </imixs-form-section>
```

The name of a analytic part is a unique key to identify the analytic data object.

To use one of the part templates you need first to implement an AnalyticHandler CDI bean that reacts on the CDI Event `AnalyticEvent`. The `AnalyticEvent` provides the following fields:

| Property | Type           | Mandatory | Description                                                |
| -------- | -------------- | --------- | ---------------------------------------------------------- |
| key      | text           | x         | A unique key to identify the analytic data set             |
| data     | ItemCollection | x         | The analytic data object represented as a `ItemCollection` |

The data object can provide any kind of data to be displayed in a custom form part. The standard data fields are:

| Property    | Type   | Mandatory | Description                             |
| ----------- | ------ | --------- | --------------------------------------- |
| value       | object |           | The analytic data value to be displayed |
| label       | text   |           | Optional label for the data             |
| description | text   |           | Optional description                    |
| link        | text   |           | Optional link for detail views          |

The value part can be any kind of data string. This could be a number a date or a complex JSON structure like it is used to display complex data in a chart diagram.

The following example shows how a custom Analytic Handler may look like:

```java
@Named
@RequestScoped
public class MyAnalyticHandler implements Serializable {

	private static final long serialVersionUID = 1L;


	public void onEvent(@Observes AnalyticEvent event) {
		// Example 1
		if ("sales.tax.totals".equals(event.getKey())) {
			event.setData(new ItemCollection().
				.setItemValue("value",computeTaxData())
				.setItemValue("label","Tax")
				.setItemValue("description","Some description"));
		}
		// Example 2
		if ("sales.order.requests".equals(event.getKey())) {
			ItemCollection analyticData=new ItemCollection().
				.setItemValue("value",computeOrderRequests(event))
				.setItemValue("label","EUR")
				.setItemValue("description","Overview orders over the last year");
			event.setData(analyticData);
			// optional persist the analytic data
			event.getWorkitem().setItemValue("order.analytic.data", analyticData.getAllItems());
		}
	}
	....
}
```

In this example the Analytic handler reacts on two analytic data sets 'tax_totals' and 'order_requests'. The collected data is returned in the AnalyticEvent data field. In the second example the data is also persisted in the current workitem. This can be used to avoid repeated complex computation of data.

The `AnalyticController` uses the data field provided by the `AnalyticEvent` and provides this data to the UX component. The following example shows a typical implementation of an Analytic form part:

```xml
<ui:composition xmlns="http://www.w3.org/1999/xhtml" xmlns:ui="http://xmlns.jcp.org/jsf/facelets"
	xmlns:f="http://xmlns.jcp.org/jsf/core" xmlns:c="http://xmlns.jcp.org/jsp/jstl/core"
	xmlns:h="http://xmlns.jcp.org/jsf/html">

	<ui:param name="data" value="#{analyticController.getData(workitem, item.name)}"></ui:param>
	<div class="dashboard-card #{data.item['class']}">
		<div class="dashboard-card-header">
			<h:outputText value="#{data.item['value']}" />
		</div>
		<div class="dashboard-card-content">
			<ui:fragment rendered="#{not empty data.item['icon']}">
				<i class="fas #{data.item['icon']}" /><span> </span>
			</ui:fragment>
			<h:outputText escape="false" value="#{data.item['label']}" />
		</div>
		<div class="dashboard-card-footer">
			<span>#{analyticController.getDescription(workitem,item.name, item.options)}</span>
			<h:panelGroup styleClass="dashboard-card-link"
				rendered="#{!empty data.item['link']}">
				<a href="#{data.item['link']}"><span
						class="typcn typcn-zoom-outline" /></a>
			</h:panelGroup>
		</div>
	</div>

</ui:composition>
```
