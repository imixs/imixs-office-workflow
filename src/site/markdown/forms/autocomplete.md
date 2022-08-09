# Autocomplete Feature

The Imixs-Office-Worklfow UI provides an autocomplete feature for input elements. This feature is based on the JavaScript library *'imixs-office.autocomplete.js'*.

## How to Integrate

To integrate and activate the feature for a input field you need at least a CDI controler bean providing a search method 


    public void searchCreditor() {
        searchResult = new ArrayList<String>();
        // get the param from faces context....
        FacesContext fc = FacesContext.getCurrentInstance();
        String phrase = fc.getExternalContext().getRequestParameterMap().get("phrase");
        if (phrase==null) {
            return;
        }
        if (phrase == null || phrase.length() < 2) {
            return;
        }
		// search....
		.....
		searchResult.add(....);
		....
    }

The searchResult should contain a List of String elements representing the suggest list.

The integration into a JSF page can look like this:


	<h:commandScript name="mySearch" action="#{myController.searchCreditor()}" 
		render="autocomplete-resultlist" onevent="autocompleteShowResult" />
	<script type="text/javascript">
		/*<![CDATA[*/
			$(document).ready(function() {
				// add autocomplete feature to cdtr.number...
				var creditorField= $("input[data-item='cdtr.number']");
				$(creditorField).each(function() {
					$(this).addClass("imixs-ml");
					autocompleteInitInput(this,mySearch,'autocomplete-resultlist',myCallback);
				});
			});
			function myCallback(selection) {
				alert(selection);
			}
		/*]]>*/
	</script>
	<h:panelGroup id="autocomplete-resultlist" layout="block" class="autocomplete-resultlist">
		<ui:repeat var="suggest" value="#{myController.searchResult}">
			<div class="autocomplete-resultlist-element" onclick="autocompleteSelectElement('#{suggest}')">
				<a href="#">
					<h:outputText value="#{suggest}" escape="false"/>
				</a>
			</div>
		</ui:repeat>
	</h:panelGroup>

	
The callback method is optional and can be used to customize the selected text block
	