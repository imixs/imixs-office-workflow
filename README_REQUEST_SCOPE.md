
# Notes

F�r den Umbau von Session Scope auf Request Scope
gen�gt es im grunde die annotation im Controller zu wechseln.

## $Uniqueid als Hidden Feld

Technisch gesehen muss jetzt aber die Maske immer daf�r sorgen,
das das Feld $unqiueid als hidden feld exisiter. 
<h:inputText value="#{workflowController.workitem.item['$uniqueid']}"></h:inputText>



## Problem 1 - Subformen

Allerdings funktioniert unsere Subform-Mechanik dann nicht mehr. Wir m�ssen diese
im Workitem total umbauen.

Der Grund d�rft mit sicherheit in der Konstruktion c:for-each liegen. 
Siehe form_panel.xhtml:

<c:forEach items="#{workflowController.editorSections}" var="section">
		<div class="imixs-form-panel">
			<h1>
				<h:outputText value="#{section.name}" />
			</h1>
			<f:subview>
				<ui:include src="/pages/workitems/forms/#{section.url}.xhtml">
					<ui:param name="workitem" value="#{workflowController.workitem}" />
				</ui:include>
			</f:subview>
		</div>
	</c:forEach>
	

	
	
# Umbau in ein 'Action Based Framework'
Um JSF als eine Art Action-Based Framework nutzen zu k�nnen sind einige Punkte zu beachten


1. Niemals ActionCommands f�r die Navigation verwenden. Dies erzeugt einen unn�tigen PostBack.
2. Keine Action Listener in Navigationen verwednen. Statdessen in der Zielseite �ber den ViewScopt (Siehe unten) einen Controller verankern der die Initalisierung �bernimmt.
3. Nur in tats�chlichen Formularen mit ActionCommands arbeiten um den generellen Vorteil von JSF mit Validierung/Convertierung nutzen zu k�nnen. 

Konkret sollten folgende Schritte beim Aufbau eines PageFlows  beachtet werden:

1. Ein einzelnes Formular (/pages/profile/settings.xhtml) ben�tigt einen 
   'ActionControler'. Dies ist eine CDI Bean im Request Scope. 
   Zur Initialiserung kann der ActionController �ber den f:view  Tag an das BeforePase event gebunden werden.
2. Das Formular muss das Feld "$uniqueid" bereitstellten   
3. Der Submit kann dann bei Workflows bequem �ber den WorkflowController (der auch im Requeestscope l�uft) abgewickelt werden
4. Jeder ActionCommand sollte immer als Result ein ...?faces-redirect=true mitgeben, damit ein
Posst/Redirect/Get pattern implementiert wird. 
   
   
	