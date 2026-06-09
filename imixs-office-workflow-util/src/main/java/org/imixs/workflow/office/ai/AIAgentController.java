package org.imixs.workflow.office.ai;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.imixs.ai.ImixsAIContextHandler;
import org.imixs.ai.agent.AIAgentCache;
import org.imixs.ai.agent.AIAgentOperator;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.DocumentService;
import org.imixs.workflow.faces.data.WorkflowController;

import jakarta.enterprise.context.ConversationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * JSF controller providing the agent conversation history for UI display. Reads
 * the persisted context from the current workitem and filters out internal
 * messages (system prompt, tool calls, tool results).
 */
@Named("aiAgentController")
@ConversationScoped
public class AIAgentController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(AIAgentController.class.getName());

    @Inject
    protected WorkflowController workflowController;

    @Inject
    AIAgentCache aiAgentCache;

    @Inject
    DocumentService documentService;

    private List<ItemCollection> cachedOperatorWorkitems = null;

    /**
     * Returns the current ai agent status form the AgentCache
     * 
     * @return
     */
    public String getAgentStatus() {

        ItemCollection source = resolveContextSource();
        if (source == null) {
            return AIAgentOperator.AGENT_STATUS_UNDEFINED;
        }
        String status = source.getItemValueString(AIAgentOperator.ITEM_AGENT_STATUS);
        return status;
    }

    /**
     * Returns the conversation history for UI display. The method first checks the
     * {@link AIAgentCache} for a live version of the current workitem. If a cached
     * version exists, it is used to read the conversation context — ensuring that
     * the chat history is up to date while the agent loop is still running. If no
     * cached version exists, the persisted workitem from the
     * {@link WorkflowController} is used as fallback.
     * <p>
     * System messages, tool calls and tool results are filtered out — only user
     * messages and plain-text assistant responses are included.
     *
     * @return list of {@link ItemCollection} instances with chat.role and
     *         chat.message
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<ItemCollection> getChatHistory() {
        List<ItemCollection> result = new ArrayList<>();

        ItemCollection source = resolveContextSource();
        if (source == null) {
            return result;
        }

        String contextItem = source.getItemValueString(AIAgentOperator.AGENT_CONFIG_CONTEXT_ITEM);
        List<Object> contextItems = source.getItemValue(contextItem);

        for (Object entry : contextItems) {
            if (!(entry instanceof Map)) {
                continue;
            }
            ItemCollection message = new ItemCollection((Map) entry);
            String role = message.getItemValueString(ImixsAIContextHandler.ITEM_ROLE);

            // Skip system messages — internal only
            if (ImixsAIContextHandler.ROLE_SYSTEM.equals(role)) {
                continue;
            }
            // Skip tool results — internal only
            if (ImixsAIContextHandler.ROLE_TOOL.equals(role)) {
                continue;
            }
            // Skip assistant tool call messages — internal only
            if (ImixsAIContextHandler.ROLE_ASSISTANT.equals(role)
                    && message.getItemValueBoolean("chat.is_tool_call")) {
                continue;
            }

            result.add(message);
        }
        return result;
    }

    /**
     * Returns the conversation history in reverse order for UI display.
     *
     * @return list of {@link ItemCollection} instances in reverse chronological
     *         order
     */
    public List<ItemCollection> getChatHistoryReverse() {
        List<ItemCollection> result = getChatHistory();
        Collections.reverse(result);
        return result;
    }

    /**
     * Returns the tool call history for UI display. The method first checks the
     * {@link AIAgentCache} for a live version of the current workitem. If a cached
     * version exists, it is used to read the conversation context — ensuring that
     * tool call results are visible in the UI while the agent loop is still
     * running. If no cached version exists, the persisted workitem from the
     * {@link WorkflowController} is used as fallback.
     * <p>
     * Only messages with role {@code tool} are included in the result.
     *
     * @return list of {@link ItemCollection} instances representing tool results
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<ItemCollection> getToolCallHistory() {
        List<ItemCollection> result = new ArrayList<>();

        ItemCollection source = resolveContextSource();
        if (source == null) {
            return result;
        }

        String contextItem = source.getItemValueString(AIAgentOperator.AGENT_CONFIG_CONTEXT_ITEM);
        List<Object> contextItems = source.getItemValue(contextItem);

        for (Object entry : contextItems) {
            if (!(entry instanceof Map)) {
                continue;
            }
            ItemCollection message = new ItemCollection((Map) entry);
            if (ImixsAIContextHandler.ROLE_TOOL.equals(
                    message.getItemValueString(ImixsAIContextHandler.ITEM_ROLE))) {
                result.add(message);
            }
        }
        return result;
    }

    /**
     * Returns a list of all workitems created by the agent task. Results are cached
     * within the conversation scope and only reloaded when the reference list size
     * changes.
     *
     * @return list of operator workitems
     */
    public List<ItemCollection> getOperatorWorkitems() {
        ItemCollection source = resolveContextSource();
        if (source == null) {
            return Collections.emptyList();
        }

        List<String> refList = workflowController.getWorkitem()
                .getItemValueList(AIAgentOperator.ITEM_AGENT_WORKITEM_REF, String.class);

        // Return cached result if reference list has not changed
        if (cachedOperatorWorkitems != null) {
            return cachedOperatorWorkitems;
        }

        // Reload from DocumentService
        List<ItemCollection> result = new ArrayList<>();
        for (String refID : refList) {
            ItemCollection doc = documentService.load(refID);
            if (doc != null) {
                result.add(doc);
            }
        }

        cachedOperatorWorkitems = result;
        return cachedOperatorWorkitems;
    }

    /**
     * Resolves the effective workitem source for context reading. Returns the
     * cached version of the current workitem if available in the
     * {@link AIAgentCache} — ensuring real-time data during an active agent loop.
     * Falls back to the persisted workitem from the {@link WorkflowController} if
     * no cache entry exists.
     *
     * @return the effective {@link ItemCollection} source, or {@code null} if no
     *         workitem is available
     */
    private ItemCollection resolveContextSource() {
        ItemCollection workitem = workflowController.getWorkitem();
        if (workitem == null) {
            return null;
        }
        ItemCollection cached = aiAgentCache.getAgentWorkitem(workitem.getUniqueID());
        return cached != null ? cached : workitem;
    }
}