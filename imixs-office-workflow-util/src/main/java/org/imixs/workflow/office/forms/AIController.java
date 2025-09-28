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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.imixs.ai.ImixsAIContextHandler;
import org.imixs.ai.workflow.OpenAIAPIConnector;
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
import jakarta.json.JsonArray;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

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

	// List<ItemCollection> chatHistory;
	String currentStreamResult = "";
	// String question = null;
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

	@Inject
	OpenAIAPIConnector openAIAPIConnector;

	@Inject
	private ImixsAIContextHandler aiMessageHandler;

	@Inject
	AIService aiService;

	private CompletableFuture<Void> streamingFuture;

	/**
	 * Returns the chat history in reverse order. Used by the frontend component
	 * 
	 * @return
	 */
	public List<ItemCollection> getChatHistory() {
		if (aiMessageHandler.getContext() == null) {
			aiMessageHandler.importContext(workflowController.getWorkitem(), AI_CHAT_HISTORY);
		}
		return aiMessageHandler.getContext();
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
			aiMessageHandler.importContext(workflowController.getWorkitem(), AI_CHAT_HISTORY);
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
		String question = workitem.getItemValueString("ai.chat.prompt").trim();
		if (question.isEmpty()) {
			// no op
			return;
		}
		logger.fine("question: " + question);

		// build context?
		if (aiMessageHandler.getContext().size() == 0) {
			String context = buildContextPrompt();
			aiMessageHandler.addSystemMessage(context);
		}
		aiMessageHandler.addQuestion(question, loginController.getUserPrincipal(), new Date())
				.setOption("stream", true);

		// starting async http request...
		streamingFuture = CompletableFuture.runAsync(() -> {
			try {
				streamPromptCompletion();
			} catch (PluginException e) {
				logger.severe("Error during streaming: " + e.getMessage());
			}
		});
		// clear input
		workflowController.getWorkitem().setItemValue("ai.chat.prompt", "");
	}

	/**
	 * Stream a prompt....
	 * 
	 */
	private void streamPromptCompletion() throws PluginException {
		try {
			HttpURLConnection conn = openAIAPIConnector.createHttpConnection(aiService.getServiceEndpoint(),
					OpenAIAPIConnector.ENDPOINT_URI_COMPLETIONS);
			conn.setRequestProperty("Accept", "text/event-stream");

			// Write the JSON object to the output stream
			String jsonString = aiMessageHandler.toString();
			logger.info("JSON Object=" + jsonString);

			try (OutputStream os = conn.getOutputStream()) {
				byte[] input = jsonString.getBytes(StandardCharsets.UTF_8);
				os.write(input, 0, input.length);
			}

			currentStreamResult = "";

			// Reading the response
			int responseCode = conn.getResponseCode();
			logger.info("POST Response Code: " + responseCode);

			if (responseCode == HttpURLConnection.HTTP_OK) {
				try (BufferedReader br = new BufferedReader(
						new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
					String line;

					while ((line = br.readLine()) != null) {
						// Check for server-sent event data lines
						if (line.startsWith("data: ")) {
							String jsonData = line.substring(6).trim();

							// Skip the [DONE] marker that indicates end of stream
							// This mark is deprecated but may be send from some LLMs.
							if ("[DONE]".equals(jsonData)) {
								logger.info("Stream completed - [DONE] received");
								break;
							}

							try {
								// Parse the JSON chunk
								JsonObject responseObject = Json.createReader(new StringReader(jsonData)).readObject();

								// Extract choices array
								JsonArray choices = responseObject.getJsonArray("choices");
								if (choices != null && choices.size() > 0) {
									JsonObject choice = choices.getJsonObject(0);

									// Check if this chunk contains content in delta
									if (choice.containsKey("delta")) {
										JsonObject delta = choice.getJsonObject("delta");

										// Extract content from delta if present
										if (delta.containsKey("content")) {
											String content = delta.getString("content");
											currentStreamResult = currentStreamResult + content;
											logger.fine("Received content: " + content);
											logger.fine("Full response so far: " + currentStreamResult);
										}
									}

									// Check for finish_reason to determine if stream is complete
									if (choice.containsKey("finish_reason") &&
											choice.get("finish_reason") != JsonValue.NULL) {
										String finishReason = choice.getString("finish_reason");
										logger.info("Stream finished with reason: " + finishReason);
										break;
									}
								}

							} catch (JsonException e) {
								logger.warning("Failed to parse JSON chunk: " + jsonData + " - " + e.getMessage());
								// Continue processing other chunks even if one fails
							}
						}
						// Handle empty lines or other SSE format lines
						else if (line.trim().isEmpty() || line.startsWith(":")) {
							// Ignore empty lines and comments in SSE format
							continue;
						}
					}

					// Set final answer after stream completion
					answer = currentStreamResult.trim();
					logger.info("Final answer: " + answer);
					aiMessageHandler.addAnswer(answer);
					// workflowController.getWorkitem().setItemValue("ai.chat.prompt", "");

					// Reset stream result
					currentStreamResult = AI_STREAM_EOS;

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
			if (aiMessageHandler != null) {
				aiMessageHandler.storeContext();
			}
		}
		return currentStreamResult;
	}

	/**
	 * This helper method creates a complex prompt containing the chronicle data
	 * 
	 * The prompt finishes with the given question.
	 * 
	 * @param question
	 * @return
	 */
	private String buildContextPrompt() {

		ItemCollection workitem = workflowController.getWorkitem();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

		// String prompt = "[INST]";
		// String prompt = "<s>";
		String prompt = "";

		prompt += "Geschäftsprozess: " + workitem.getWorkflowGroup() + "\n";
		prompt += "Erstellt: " + dateFormat.format(workitem.getItemValueDate(WorkflowKernel.CREATED)) + " von "
				+ userController.getUserName(workitem.getItemValueString("$creator")) + " \n";
		prompt += "Aktueller Status: " + workitem.getItemValueString(WorkflowKernel.WORKFLOWSTATUS) + "\n";
		prompt += "\nVerlauf an Aktivitäten in diesem Geschäftsprozess: \n\n";

		// chronical
		List<Integer> years = chronicleController.getYears();
		Collections.reverse(years);
		for (int year : years) {
			List<Integer> months = chronicleController.getMonths(year);
			Collections.reverse(months);
			for (int month : months) {
				List<ChronicleEntity> chronicleEntries = chronicleController.getChroniclePerMonth(year, month);

				Collections.reverse(chronicleEntries);

				// prompt += "\n" + year + "/" + month + "\n\n";
				for (ChronicleEntity entry : chronicleEntries) {
					String user = userController.getUserName(entry.getUser());
					List<ItemCollection> cronicleEvents = entry.entries;
					Collections.reverse(cronicleEvents);
					for (ItemCollection event : cronicleEvents) {
						// Date / User
						prompt += dateFormat.format(entry.getDate()) + " - " + user + ": ";
						String type = event.getItemValueString("type");

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
						if ("imixs-ai".equals(type)) {
							prompt += "Chat: " + message + "\n";
						}
					}
				}
			}
		}

		System.out.println(prompt);
		return prompt;
	}

}
