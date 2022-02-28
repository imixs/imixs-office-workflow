# Search

Imixs-Office-Workflow provides a rich search feature by providing different search forms. 

## Search Page

The size of the result page can be configured by imixs.properties:

	"office.search.pagesize", defaultValue = "10"


## Extending Custom Search Filter

The search filter fomr can be extended by custom search filters. For that the page `worklist_search_custom.xhtml` can be overwritten with additional search filters. See the following example:

	<ui:composition xmlns="http://www.w3.org/1999/xhtml"
		xmlns:f="http://xmlns.jcp.org/jsf/core"
		xmlns:h="http://xmlns.jcp.org/jsf/html"
		xmlns:c="http://xmlns.jcp.org/jsp/jstl/core"
		xmlns:ui="http://xmlns.jcp.org/jsf/facelets"
		xmlns:a="http://xmlns.jcp.org/jsf/passthrough">
	
		<div class="imixs-form-section-2">
			<dl>
				<dt>Rechnungsbetrag:</dt>
				<dd>
					<strong>#{message.from}: </strong>
					<h:inputText style="width:12em;"
						value="#{searchController.searchFilter.item['_amount_brutto_from']}">
						<f:convertNumber minFractionDigits="2" locale="de" />
					</h:inputText>
					<strong>#{message.to}: </strong>
					<h:inputText style="width:12em;"
						value="#{searchController.searchFilter.item['_amount_brutto_to']}">
						<f:convertNumber minFractionDigits="2" locale="de" />
					</h:inputText>
				</dd>
			</dl>
		</div>
	</ui:composition>


To extend the search query computed by Imixs-Office-Workflow a custom CDI Bean can react on the SearchQuery event and provide an additional search filter. See the following example:



	@Named
	@ConversationScoped
	public class CustomSearchController implements Serializable {
	    private static final long serialVersionUID = 1L;
	
	    public void onSearchEvent(@Observes SearchEvent searchEvent) {
	
	        String query = searchEvent.getQuery();
	        double from = searchEvent.getSearchFilter().getItemValueDouble("_amount_brutto_from");
	        double to = searchEvent.getSearchFilter().getItemValueDouble("_amount_brutto_to");
	        if (to != 0.0 || from != 0.0) {
	            if (to == 0.0) {
	                to = 99999999999.99;
	            }
	            query += " (_amount_brutto:[" + from + " TO " + to + "])";
	            searchEvent.setQuery(query);
	        }
	    }
	}


**NOTE:** If you add additional search criteria you need possible to extend also the imixs.property `index.fields.noanalyze` and rebuild the search index.


