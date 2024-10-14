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
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.imixs.ai.workflow.OpenAIAPIService;
import org.imixs.marty.profile.UserController;
import org.imixs.workflow.FileData;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.WorkflowKernel;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.faces.data.WorkflowController;
import org.imixs.workflow.faces.data.WorkflowEvent;
import org.imixs.workflow.faces.util.LoginController;

import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.Json;
import jakarta.json.JsonObject;

/**
 * The AIController integrates the imixs-ai module providing a AI Chat history.
 * The method 'sendAsync' can be used to stream a prompt. This feature is used
 * by the front end component 'workitem.ai.xhtml' to display the life-answering.
 * After the answer was received completely the method automatically updates
 * the workitem ''
 * 
 * The method buildContextPrompt creates a complex prompt based on a given
 * question and the workflow information stored in the current workitem.
 * 
 * 
 * @see workitem_chronicle.xhtml
 * @author rsoika
 */
@Named("aiController")
@ConversationScoped
public class AIController implements Serializable {
	public static final String ERROR_PROMPT_TEMPLATE = "ERROR_PROMPT_TEMPLATE";
	public static final String ERROR_PROMPT_INFERENCE = "ERROR_PROMPT_INFERENCE";
	public static final String AI_CHAT_HISTORY = "ai.chat.history";

	public static final String AI_STREAM_EOS = "<!-- imixs.ai.stream.completed -->";

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(AIController.class.getName());

	List<ItemCollection> chatHistory;
	String currentStreamResult = "";
	String question = null;
	String answer = null;

	@Inject
	protected WorkflowController workflowController;

	@Inject
	protected ChronicleController chronicleController;

	@Inject
	protected UserController userController;

	@Inject
	protected LoginController loginController;

	@Inject
	OpenAIAPIService openAIAPIService;

	private CompletableFuture<Void> streamingFuture;

	/**
	 * Returns the chat history in reverse order. Used by the frontend component
	 * 
	 * @return
	 */
	public List<ItemCollection> getChatHistory() {
		List<ItemCollection> reversedList = new ArrayList<>(chatHistory);
		Collections.reverse(reversedList);
		return reversedList;
	}

	/**
	 * WorkflowEvent listener
	 * 
	 * If a new WorkItem was created or changed, the imixs.ai.chat.history view will
	 * be initialized or updated.
	 * 
	 * @param workflowEvent
	 */
	public void onWorkflowEvent(@Observes WorkflowEvent workflowEvent) {
		if (workflowEvent == null) {
			return;
		}
		if (WorkflowEvent.WORKITEM_CREATED == workflowEvent.getEventType()
				|| WorkflowEvent.WORKITEM_CHANGED == workflowEvent.getEventType()) {
			// read current imixs.ai.chat.history...
			chatHistory = ChildItemController.explodeChildList(workflowController.getWorkitem(), AI_CHAT_HISTORY);
		}

	}

	/**
	 * Sends an asynchronous prompt
	 * 
	 * 
	 * '{"prompt": "Building a website","n_predict": 128}'
	 */
	public void sendAsync() throws PluginException {
		ItemCollection workitem = workflowController.getWorkitem();
		question = workitem.getItemValueString("ai.chat.prompt");
		logger.fine("question: " + question);
		String prompt = buildContextPrompt(question);
		JsonObject jsonPrompt = openAIAPIService.buildJsonPromptObject(prompt, true, null);

		// starting async http request...
		streamingFuture = CompletableFuture.runAsync(() -> {
			try {
				streamPromptCompletion(jsonPrompt);
			} catch (PluginException e) {
				logger.severe("Error during streaming: " + e.getMessage());
			}
		});
	}

	/**
	 * Stream a prompt....
	 * 
	 */
	private void streamPromptCompletion(JsonObject jsonPromptObject) throws PluginException {
		try {

			HttpURLConnection conn = openAIAPIService.createHttpConnection(null);
			conn.setRequestProperty("Accept", "text/event-stream");

			// Write the JSON object to the output stream
			String jsonString = jsonPromptObject.toString();
			logger.fine("JSON Object=" + jsonString);

			try (OutputStream os = conn.getOutputStream()) {
				byte[] input = jsonString.getBytes(StandardCharsets.UTF_8);
				os.write(input, 0, input.length);
			}
			currentStreamResult = "";
			// Reading the response
			int responseCode = conn.getResponseCode();
			logger.fine("POST Response Code: " + responseCode);
			if (responseCode == HttpURLConnection.HTTP_OK) {
				try (BufferedReader br = new BufferedReader(
						new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
					String line;
					// StringBuilder fullResponse = new StringBuilder();
					while ((line = br.readLine()) != null) {
						if (line.startsWith("data: ")) {
							String jsonData = line.substring(6);
							JsonObject responseObject = Json.createReader(new StringReader(jsonData)).readObject();
							String content = responseObject.getString("content");
							boolean stop = responseObject.getBoolean("stop", false);
							currentStreamResult = currentStreamResult + content;
							logger.fine("FullResponse: " + currentStreamResult);
							if (stop) {
								logger.fine("request completed - adding answer....");
								answer = currentStreamResult.trim();

								// reset stream result!
								currentStreamResult = AI_STREAM_EOS;
								break;
							}
						}
					}
				}
			} else {
				throw new PluginException(AIController.class.getSimpleName(),
						ERROR_PROMPT_INFERENCE, "Error during POST prompt: HTTP Result " + responseCode);
			}
			conn.disconnect();
		} catch (IOException e) {
			logger.severe(e.getMessage());
			throw new PluginException(AIController.class.getSimpleName(), ERROR_PROMPT_TEMPLATE,
					"Exception during POST prompt - " + e.getMessage(), e);
		}
	}

	public boolean isStreamingComplete() {
		return streamingFuture != null && streamingFuture.isDone();
	}

	public String getStreamResult() {
		if (AI_STREAM_EOS.equals(currentStreamResult)) {
			updateChatHistory();
		}
		return currentStreamResult;

	}

	/**
	 * Helper method to update the ai.chat.history
	 */
	private void updateChatHistory() {
		ItemCollection chatEntry = new ItemCollection();
		chatEntry.setItemValue("question", question);
		chatEntry.setItemValue("answer", answer);
		chatEntry.setItemValue("date", new Date());
		chatEntry.setItemValue("user", loginController.getUserPrincipal());
		chatHistory.add(chatEntry);
		// persist new imixs.ai.chat.history
		ChildItemController.implodeChildList(workflowController.getWorkitem(), chatHistory, AI_CHAT_HISTORY);
		workflowController.getWorkitem().removeItem("ai.chat.prompt");
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
