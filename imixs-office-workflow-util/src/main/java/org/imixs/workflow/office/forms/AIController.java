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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.microprofile.config.inject.ConfigProperty;
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
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;

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

	public static final String LLM_SERVICE_ENDPOINT = "llm.service.endpoint";
	public static final String ENV_LLM_SERVICE_ENDPOINT_USER = "llm.service.endpoint.user";
	public static final String ENV_LLM_SERVICE_ENDPOINT_PASSWORD = "llm.service.endpoint.password";
	public static final String ENV_LLM_SERVICE_ENDPOINT_TIMEOUT = "LLM_SERVICE_TIMEOUT";

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(AIController.class.getName());

	List<String> chatHistory;

	@Inject
	protected WorkflowController workflowController;

	@Inject
	protected ChronicleController chronicleController;

	@Inject
	protected UserController userController;

	@Inject
	@ConfigProperty(name = LLM_SERVICE_ENDPOINT, defaultValue = "a")
	String serviceEndpoint;

	@Inject
	@ConfigProperty(name = ENV_LLM_SERVICE_ENDPOINT_USER, defaultValue = "b")
	String serviceEndpointUser;

	@Inject
	@ConfigProperty(name = ENV_LLM_SERVICE_ENDPOINT_PASSWORD, defaultValue = "c")
	String serviceEndpointPassword;

	@Inject
	@ConfigProperty(name = ENV_LLM_SERVICE_ENDPOINT_TIMEOUT, defaultValue = "120000")
	int serviceTimeout;

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

		String input = workflowController.getWorkitem().getItemValueString("ai.chat.prompt");
		logger.info("prompt...:" + input);

		JsonObject jsonPrompt = buildJsonPromptObject(input, null);

		String result = postPromptCompletion(serviceEndpoint, jsonPrompt);
		logger.info("result=" + result);

		processPromptResult(result, workflowController.getWorkitem());

	}

	/**
	 * This method POST a given prompt to the endpoint '/completion' and returns the
	 * predicted completion.
	 * The method returns the response body.
	 * 
	 * The method optional test if the environment variables
	 * LLM_SERVICE_ENDPOINT_USER and LLM_SERVICE_ENDPOINT_PASSWORD are set. In this
	 * case a BASIC Authentication is used for the connection to the LLMService.
	 * 
	 * See details:
	 * https://github.com/ggerganov/llama.cpp/blob/master/examples/server/README.md#api-endpoints
	 * 
	 * 
	 * curl example:
	 * 
	 * curl --request POST \
	 * --url http://localhost:8080/completion \
	 * --header "Content-Type: application/json" \
	 * --data '{"prompt": "Building a website can be done in 10 simple
	 * steps:","n_predict": 128}'
	 * 
	 * @param xmlPromptData
	 * @throws PluginException
	 */
	public String postPromptCompletion(String apiEndpoint, JsonObject jsonPromptObject)
			throws PluginException {
		String response = null;
		try {
			if (!apiEndpoint.endsWith("/")) {
				apiEndpoint = apiEndpoint + "/";
			}
			URL url = new URL(apiEndpoint + "completion");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setConnectTimeout(serviceTimeout); // set timeout to 5 seconds
			conn.setReadTimeout(serviceTimeout);
			// Set Basic Authentication?
			if (serviceEndpointUser != null && !serviceEndpointUser.isEmpty()
					&& !serviceEndpointPassword.isEmpty()) {
				String auth = serviceEndpointUser + ":" + serviceEndpointPassword;
				byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
				String authHeaderValue = "Basic " + new String(encodedAuth);
				conn.setRequestProperty("Authorization", authHeaderValue);
			}

			// Set the appropriate HTTP method
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json; utf-8");
			conn.setRequestProperty("Accept", "application/json");
			conn.setDoOutput(true);

			// Write the JSON object to the output stream
			String jsonString = jsonPromptObject.toString();
			logger.fine("JSON Object=" + jsonString);

			try (OutputStream os = conn.getOutputStream()) {
				byte[] input = jsonString.getBytes(StandardCharsets.UTF_8);
				os.write(input, 0, input.length);
			}

			// Reading the response
			int responseCode = conn.getResponseCode();
			logger.fine("POST Response Code :: " + responseCode);
			if (responseCode == HttpURLConnection.HTTP_OK) {
				try (BufferedReader br = new BufferedReader(
						new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
					StringBuilder responseBody = new StringBuilder();
					String responseLine = null;
					while ((responseLine = br.readLine()) != null) {
						responseBody.append(responseLine.trim() + "\n");
					}
					response = responseBody.toString();
					logger.fine("Response Body :: " + response);
				}
			} else {
				throw new PluginException(AIController.class.getSimpleName(),
						ERROR_PROMPT_INFERENCE, "Error during POST prompt: HTTP Result " + responseCode);
			}
			// Close the connection
			conn.disconnect();
			logger.fine("===== postPromptCompletion completed");
			return response;

		} catch (IOException e) {
			logger.severe(e.getMessage());
			throw new PluginException(
					AIController.class.getSimpleName(),
					ERROR_PROMPT_TEMPLATE,
					"Exception during POST prompt - " + e.getClass().getName() + ": " + e.getMessage(), e);
		}

	}

	/**
	 * This helper method builds a json prompt object including options params.
	 * 
	 * See details:
	 * https://github.com/ggerganov/llama.cpp/blob/master/examples/server/README.md#api-endpoints
	 * 
	 * @param prompt
	 * @param prompt_options
	 * @return
	 */
	public JsonObject buildJsonPromptObject(String question, String prompt_options) {

		// Create a JsonObjectBuilder instance
		JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();

		String contextPrompt = buildContextPrompt(question);
		System.out.println(contextPrompt);
		jsonObjectBuilder.add("prompt", contextPrompt);

		// Do we have options?
		if (prompt_options != null && !prompt_options.isEmpty()) {
			// Create a JsonReader from the JSON string
			JsonReader jsonReader = Json.createReader(new StringReader(prompt_options));
			JsonObject parsedJsonObject = jsonReader.readObject();
			jsonReader.close();
			// Add each key-value pair from the parsed JsonObject to the new
			// JsonObjectBuilder
			for (Map.Entry<String, JsonValue> entry : parsedJsonObject.entrySet()) {
				jsonObjectBuilder.add(entry.getKey(), entry.getValue());
			}
		}

		// Build the JsonObject
		JsonObject jsonObject = jsonObjectBuilder.build();

		logger.fine("buildJsonPromptObject completed:");
		logger.fine(jsonObject.toString());
		return jsonObject;
	}

	/**
	 * This helper method creates a complex prompt containing the chronical data
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

		prompt += "[/INST]</s>\n[INST] " + question + "[/INST]";
		return prompt;

	}

	/**
	 * This method processes a OpenAI API prompt result in JSON format. The method
	 * expects a workitem* including the item 'ai.result' providing the LLM result
	 * string.
	 * 
	 * The parameter 'resultItemName' defines the item to store the result string.
	 * This param can be empty.
	 * 
	 * The parameter 'mode' defines a resolver method.
	 * 
	 * @param workitem        - the workitem holding the last AI result (stored in a
	 *                        value
	 *                        list)
	 * @param resultItemName  - the item name to store the llm text result
	 * @param resultEventType - optional event type send to all CDI Event observers
	 *                        for the LLMResultEvent
	 * @throws PluginException
	 */
	public void processPromptResult(String completionResult, ItemCollection workitem) throws PluginException {

		// We expect a OpenAI API Json Result object
		// Extract the field 'content'
		// Create a JsonReader from the JSON string
		JsonReader jsonReader = Json.createReader(new StringReader(completionResult));
		JsonObject parsedJsonObject = jsonReader.readObject();
		jsonReader.close();

		// extract content
		String promptResult = parsedJsonObject.getString("content");
		if (promptResult == null) {
			throw new PluginException(AIController.class.getSimpleName(),
					ERROR_PROMPT_INFERENCE, "Error during POST prompt - no result returned!");
		}
		promptResult = promptResult.trim();

		logger.info("Result=" + promptResult);
		chatHistory.add(promptResult);

	}
}
