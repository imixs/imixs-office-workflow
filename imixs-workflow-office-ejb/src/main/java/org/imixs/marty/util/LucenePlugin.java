package org.imixs.marty.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Logger;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.Plugin;
import org.imixs.workflow.WorkflowContext;
import org.imixs.workflow.plugins.AbstractPlugin;

/**
 * This Plugin add workitems to a lucene search index
 * 
 * @author rsoika
 * 
 */
public class LucenePlugin extends AbstractPlugin {
	Properties properties = null;
	IndexWriter writer = null;
	static List<String> searchFields = null;
	private static Logger logger = Logger.getLogger("org.imixs.workflow");


	@Override
	public void init(WorkflowContext actx) throws Exception {

		super.init(actx);

		// try loading imixs-search properties

		properties = new Properties();
		try {
			properties.load(Thread.currentThread().getContextClassLoader()
					.getResource("imixs-search.properties").openStream());
		} catch (Exception ep) {
			// no properties found
		}

		String sIndexDir = properties.get("IndexDir") + "";
		String sFieldList = properties.get("FieldList") + "";

		logger.info("IndexDir:" + sIndexDir);
		logger.info("FieldList:" + sFieldList);

		// compute search field list
		StringTokenizer st = new StringTokenizer(sFieldList, ",");
		searchFields = new ArrayList<String>();
		while (st.hasMoreElements()) {
			String sName = st.nextToken().toLowerCase();
			// do not add internal fields
			if (!"$uniqueid".equals(sName) && !"$readaccess".equals(sName))
				searchFields.add(sName);
		}
		// initialize lucene index writer
		File indexDir = new File(sIndexDir);
		writer = new IndexWriter(indexDir, new StandardAnalyzer(), true);
	}

	public int run(ItemCollection documentContext, ItemCollection activity)
			throws Exception {
		if (properties.isEmpty())
			return Plugin.PLUGIN_OK;
		else {
			// add workitem to search index....

			writer.addDocument(createDocument(documentContext));

			logger.info(" add document successfull");
		}
		return Plugin.PLUGIN_OK;
	}

	public void close(int status) throws Exception {
		logger.info(" close writer");
		writer.optimize();
		writer.close();
		
		
		// jetzt ein kleiner suchtest
		
		
	}

	public static Document createDocument(ItemCollection aworkitem) {
		String sValue = null;
		Document doc = new Document();

		// add each field from the search field list into the lucene document
		for (String aFieldname : searchFields) {
			sValue = aworkitem.getItemValueString(aFieldname);
			logger.info("  add Field: "+aFieldname + " = " + sValue);
			doc.add(new Field(aFieldname, sValue, Field.Store.NO,
					Field.Index.UN_TOKENIZED));

		}

		// add default values
		doc.add(new Field("$uniqueid", aworkitem
				.getItemValueString("$uniqueid"), Field.Store.YES,
				Field.Index.NO));

		Vector<String> vReadAccess = aworkitem.getItemValue("$readAccess");
		sValue = "";
		for (String sReader : vReadAccess)
			sValue += sReader + ",";

		doc.add(new Field("$readaccess", sValue, Field.Store.YES,
				Field.Index.TOKENIZED));

		return doc;
	}

}
