package org.imixs.workflow.office.textadapter;

import java.util.List;
import java.util.logging.Logger;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.TextEvent;
import org.imixs.workflow.faces.data.WorkflowController;
import org.imixs.workflow.office.forms.WorkitemLinkController;
import org.imixs.workflow.util.XMLParser;

import jakarta.ejb.Stateless;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

/**
 * The TextAdapterWorkitemRef replaces text fragments with the tag
 * <workitemref>..</workitemref>. The item name of this tag will be replaced
 * with the iteam value from the corresponding workitem.
 * <p>
 * Example:
 * <p>
 * {@code
 *    
 *    Plant ID : <workitemref filter="xxx">plant.id</workitemref>.
 * }
 * <p>
 * This will replace the tag with the item value of plant.id from the reffered
 * workitem.
 * <p>
 * 
 * 
 * @author rsoika
 *
 */
@Stateless
public class WorkitemRefAdapter {

    private static Logger logger = Logger.getLogger(WorkitemRefAdapter.class.getName());

    @Inject
    protected WorkflowController workflowController;

    @Inject
    protected WorkitemLinkController workitemLinkController;

    /**
     * This method reacts on CDI events of the type TextEvent and parses a string
     * for xml tag <username>. Those tags will be replaced with the corresponding
     * display name of the user profile.
     * 
     */
    public void onEvent(@Observes TextEvent event) {
        String text = event.getText();
        String textResult = "";

        if (text == null)
            return;

        List<String> tagList = XMLParser.findTags(text, "workitemref");
        logger.finest(tagList.size() + " tags found");
        // test if a <value> tag exists...
        for (String tag : tagList) {
            // next we check if the start tag contains a 'separator' attribute
            String filter = XMLParser.findAttribute(tag, "filter");

            List<ItemCollection> list = workitemLinkController.getReferences(filter);
            if (list == null || list.size() == 0) {
                // no match
                return;
            }
            // simply take the first match
            ItemCollection refWorkItem = list.get(0);

            // extract Item Value
            String sItemName = XMLParser.findTagValue(tag, "workitemref");

            // use stub?
            if (refWorkItem.hasItem(sItemName)) {
                textResult = refWorkItem.getItemValueString(sItemName);
            } else {
                // load workitem
                refWorkItem = workflowController.getDocumentService().load(refWorkItem.getUniqueID());
                textResult = refWorkItem.getItemValueString(sItemName);
            }
            // // now replace the tag with the result string
            int iStartPos = text.indexOf(tag);
            int iEndPos = text.indexOf(tag) + tag.length();
            text = text.substring(0, iStartPos) + textResult + text.substring(iEndPos);

        }

        event.setText(text);
    }

}
