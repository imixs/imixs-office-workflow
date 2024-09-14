# Analytic Parts

'Analytic Parts' can be used to build dashboards and display any kind of analytic data from a business process. 
There are several part templates defined to display numbers, text, charts. 

## How to Integrate

A custom part is added to a form like all other part types. It has a key item and a label:

```xml
  <imixs-form-section columns="3" label="Analytic Data">
    <item name="tax_totals" type="custom" path="analyze_number" label="Tax:" />
    <item name="order_requests" type="custom" path="analyze_chart" label="Orders:" />
  </imixs-form-section>
```

To use one of the part templates you need first to implement a CDI bean that reacts on CDI Events from the type `AnalyticEvent` . An `AnalyticEvent`  provides the following data fields:


| Property  | Type 	  | Mandatory | Description												|
|-----------|---------|-----------|---------------------------------------------------------|
| key		| text    | x         | Name of the analytic data set                           |
| value		| text    | x         | A text value representing the data     				|
| label		| text    |           | Optional label for the data								|
| description| text   |           | Optional description     								|
		

The value part can be any kind of data string. This could be a number a date or a complex JSON structure like it is used to display complex data in a chart diagram.

This is an example how your custom Analytic Controler can look like:

```java
@Named
@RequestScoped
public class MyAnalyticController implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Inject
	protected DocumentService documentService;

	public void onEvent(@Observes AnalyticEvent event) {
		if ("tax_totals".equals(event.getKey())) {
			event.setValue(computeTaxdata());
			event.setLabel("Tax");
			event.setDescription("Some description");
			

		}
		if ("order_requests".equals(event.getKey())) {
			event.setValue(computeOrderRequests());
			event.setLabel("EUR");
			event.setDescription("Overview orders over the last year");
			event.getWorkitem().setItemValue("order.total", event.getValue());
		}

	}
}
```

In this example the Analytic controller reacts on two analytic data sets 'tax_totals' and 'order_requests'. The collected data is cached in the workitem to avoid repeated complex computation of data. 

The data fields are stored in an map-item with the given key. In the example e.g. 'tax_totals'. The map contains the items 'value', 'label', 'description' which are used to display the information on a analytic part. An Analytic controller can of course add additional data to a workitem. In the example 'order_requests' we add a custom item named 'order.total' which will not become part of the analytic part to be displayed but maybe used in the further processing flow. 


