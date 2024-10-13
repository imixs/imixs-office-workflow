/*******************************************************************************
 *  Imixs Workflow Technology
 *  Copyright (C) 2003, 2008 Imixs Software Solutions GmbH,  
 *  http://www.imixs.com
 *  
 *  This program is free software; you can redistribute it and/or 
 *  modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation; either version 2 
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 *  General Public License for more details.
 *  
 *  You can receive a copy of the GNU General Public
 *  License at http://www.gnu.org/licenses/gpl.html
 *  
 *  Contributors:  
 *  	Imixs Software Solutions GmbH - initial API and implementation
 *  	Ralph Soika
 *  
 *******************************************************************************/
package org.imixs.workflow.office.forms;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.imixs.ai.workflow.OpenAIAPIService;
import org.imixs.marty.profile.UserController;
import org.imixs.workflow.FileData;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.WorkflowKernel;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.faces.data.WorkflowController;
import org.imixs.workflow.faces.data.WorkflowEvent;

import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.JsonObject;

/**
 * The AIController integrates the imixs-ai module
 * 
 * 
 * @see workitem_chronicle.xhtml
 * @author rsoika,gheinle
 */
@Named("aiController")
@ConversationScoped
public class AIController implements Serializable {
	public static final String ERROR_PROMPT_TEMPLATE = "ERROR_PROMPT_TEMPLATE";
	public static final String ERROR_PROMPT_INFERENCE = "ERROR_PROMPT_INFERENCE";

	// public static final String LLM_SERVICE_ENDPOINT = "llm.service.endpoint";
	// public static final String ENV_LLM_SERVICE_ENDPOINT_USER =
	// "llm.service.endpoint.user";
	// public static final String ENV_LLM_SERVICE_ENDPOINT_PASSWORD =
	// "llm.service.endpoint.password";
	// public static final String ENV_LLM_SERVICE_ENDPOINT_TIMEOUT =
	// "LLM_SERVICE_TIMEOUT";

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(AIController.class.getName());

	List<String> chatHistory;

	@Inject
	protected WorkflowController workflowController;

	@Inject
	protected ChronicleController chronicleController;

	@Inject
	protected UserController userController;

	// @Inject
	// @ConfigProperty(name = LLM_SERVICE_ENDPOINT, defaultValue = "a")
	// String serviceEndpoint;

	// @Inject
	// @ConfigProperty(name = ENV_LLM_SERVICE_ENDPOINT_USER, defaultValue = "b")
	// String serviceEndpointUser;

	// @Inject
	// @ConfigProperty(name = ENV_LLM_SERVICE_ENDPOINT_PASSWORD, defaultValue = "c")
	// String serviceEndpointPassword;

	// @Inject
	// @ConfigProperty(name = ENV_LLM_SERVICE_ENDPOINT_TIMEOUT, defaultValue =
	// "120000")
	// int serviceTimeout;

	@Inject
	OpenAIAPIService openAIAPIService;

	/**
	 * This helper method is called during the WorkflowEvent.WORKITEM_CHANGED to
	 * update the chronicle view for the current workitem.
	 */
	@SuppressWarnings("unchecked")
	public void init() {
		long l = System.currentTimeMillis();
		chatHistory = new ArrayList<String>();

	}

	public List<String> getChatHistory() {
		return chatHistory;
	}

	/**
	 * WorkflowEvent listener
	 * 
	 * If a new WorkItem was created or changed, the chronicle view will be
	 * initialized.
	 * 
	 * @param workflowEvent
	 */
	public void onWorkflowEvent(@Observes WorkflowEvent workflowEvent) {
		if (workflowEvent == null) {
			return;
		}
		if (WorkflowEvent.WORKITEM_CREATED == workflowEvent.getEventType()
				|| WorkflowEvent.WORKITEM_CHANGED == workflowEvent.getEventType()) {
			// reset data...
			init();
		}
	}

	/**
	 * Sends a new prompt item
	 * 
	 * 
	 * '{"prompt": "Building a website","n_predict": 128}'
	 */
	public void send() throws PluginException {

		ItemCollection workitem = workflowController.getWorkitem();
		String input = workitem.getItemValueString("ai.chat.prompt");
		logger.info("prompt...:" + input);

		// build context....
		String prompt = buildContextPrompt(input);
		JsonObject jsonPrompt = openAIAPIService.buildJsonPromptObject(prompt, null);
		String result = openAIAPIService.postPromptCompletion(jsonPrompt, null);
		openAIAPIService.processPromptResult(result, workflowController.getWorkitem(), null, null);
		updateChatHistory(workitem);
	}

	/**
	 * This method adds the prompt result into the ai chat history.
	 * 
	 * @throws PluginException
	 */
	public void updateChatHistory(ItemCollection workitem) throws PluginException {
		// extract last prompt result
		List<String> promptResults = workitem.getItemValueList(OpenAIAPIService.ITEM_AI_RESULT, String.class);
		String promptResult = null;
		if (!promptResults.isEmpty()) {
			promptResult = promptResults.get(promptResults.size() - 1);
		}
		logger.info("Result=" + promptResult);
		chatHistory.add(promptResult);
	}

	/**
	 * This helper method creates a complex prompt containing the chronicle data
	 * 
	 * The prompt finishes with the given question.
	 * 
	 * @param question
	 * @return
	 */
	private String buildContextPrompt(String question) {

		ItemCollection workitem = workflowController.getWorkitem();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

		String prompt = "[INST]";

		prompt = "Geschäftsprozess: " + workitem.getWorkflowGroup() + "\n";
		prompt += "Erstellt: " + dateFormat.format(workitem.getItemValueDate(WorkflowKernel.CREATED)) + " von "
				+ userController.getUserName(workitem.getItemValueString("$creator")) + " \n";
		prompt += "Aktueller Status: " + workitem.getItemValueString(WorkflowKernel.WORKFLOWSTATUS) + "\n";

		// chronical

		List<Integer> years = chronicleController.getYears();
		Collections.reverse(years);
		for (int year : years) {
			List<Integer> months = chronicleController.getMonths(year);
			Collections.reverse(months);
			for (int month : months) {
				List<ChronicleEntity> chronicleEntries = chronicleController.getChroniclePerMonth(year, month);

				// prompt += "\n" + year + "/" + month + "\n\n";
				for (ChronicleEntity entry : chronicleEntries) {
					String user = userController.getUserName(entry.getUser());

					List<ItemCollection> cronicleEvents = entry.entries;
					Collections.reverse(cronicleEvents);

					for (ItemCollection event : cronicleEvents) {

						// Date / User
						prompt += dateFormat.format(entry.getDate()) + " - " + user + ": ";

						String type = event.getItemValueString("type");

						Date date = event.getItemValueDate("date");
						String message = event.getItemValueString("message");

						if ("comment".equals(type)) {
							prompt += "Kommentar: " + message + "\n";
						}
						if ("history".equals(type)) {
							prompt += message + "\n";
						}
						if ("dms".equals(type)) {
							String fileName = event.getItemValueString("name");
							FileData fileData = workitem.getFileData(fileName);
							if (fileData != null && fileData.getAttribute("text") != null) {
								String fileContent = fileData.getAttribute("text").toString();
								if (fileContent != null && !fileContent.isEmpty()) {
									prompt += "Neues Dokument hinzugefügt: " + fileName + "\n\n";
									prompt += fileContent + "\n\n";
								}
							}
						}

					}

				}
			}
		}

		prompt += "[/INST]</s>\n[INST] " + question + "[/INST]";
		return prompt;

	}

}
