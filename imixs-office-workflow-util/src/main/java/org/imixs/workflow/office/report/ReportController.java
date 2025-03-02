package org.imixs.workflow.office.report;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.imixs.workflow.FileData;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.ReportService;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.faces.data.DocumentController;
import org.imixs.workflow.faces.fileupload.FileUploadController;
import org.imixs.workflow.xml.XMLDocumentAdapter;
import org.xml.sax.SAXException;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.xml.bind.JAXBException;

@Named
@ConversationScoped
public class ReportController implements Serializable {

	private ItemCollection reportUploads;

	@Inject
	protected ReportService reportService;

	@Inject
	DocumentController documentController;

	@Inject
	FileUploadController fileUploadController;

	Map<String, String> params;

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(ReportController.class.getName());

	/**
	 * PostConstruct event - init model uploads
	 */
	@PostConstruct
	void init() {
		reportUploads = new ItemCollection();
	}

	public void reset() {
		params = null;
		reportUploads = new ItemCollection();
	}

	public ItemCollection getReportUploads() {
		return reportUploads;
	}

	public void setReportUploads(ItemCollection reportUploads) {
		this.reportUploads = reportUploads;
	}

	/**
	 * Returns a String sorted list of all report names.
	 * 
	 * @return list of report names
	 */
	public List<ItemCollection> getReports() {
		return reportService.findAllReports();
	}

	public Map<String, String> getParams() {
		logger.fine("parsing params...");
		ItemCollection report = documentController.getDocument();
		if (params == null && report != null) {
			params = new LinkedHashMap<String, String>();

			// parse query
			String query = report.getItemValueString("txtquery");
			int i = 0;
			while ((i = query.indexOf('{', i)) > -1) {
				// cut next } :
				int j = query.indexOf('}', i);
				if (j > -1) {
					// cut here!
					String sKey = query.substring(i + 1, j);
					params.put(sKey, "");
					i = j;
				}

			}

			// provide old param setting with ?
			// parse query
			i = 0;
			boolean found = false;
			while ((i = query.indexOf('?', i)) > -1) {
				String sTest = query.substring(i + 1);
				// cut next space or ' or " or ] or :
				for (int j = 0; j < sTest.length(); j++) {
					char c = sTest.charAt(j);
					if (c == '\'' || c == '"' || c == ']' || c == ':' || c == ' ' || c == ')') {
						// cut here!
						String sKey = query.substring(i + 1, i + j + 1);
						logger.warning("...detected old report param format. Replace ?xxx with {xxx}");
						params.put(sKey, "");
						i++;
						found = true;
						break;
					}
				}
				// we did not found a parameter end char!
				if (!found) {
					logger.warning("...unable to parse report param format. Replace ?xxx with {xxx}");
					break;
				}

			}

		}
		return params;
	}

	/**
	 * This method adds all uploaded imixs-report files.
	 * 
	 * @param event
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws ParseException
	 * @throws ModelException
	 * @throws JAXBException
	 * 
	 */
	public void uploadReport() throws ModelException, ParseException, ParserConfigurationException, SAXException,
			IOException, JAXBException {

		try {
			fileUploadController.attacheFiles(reportUploads);
		} catch (PluginException e) {
			e.printStackTrace();
		}

		List<FileData> fileList = getReportUploads().getFileData();

		if (fileList == null) {
			return;
		}
		for (FileData file : fileList) {
			logger.info("Import report: " + file.getName());

			// test if imxis-report?
			if (file.getName().endsWith(".imixs-report")) {
				ByteArrayInputStream input = new ByteArrayInputStream(file.getContent());
				ItemCollection report = XMLDocumentAdapter.readItemCollectionFromInputStream(input);
				reportService.updateReport(report);
				continue;
			}
			// model type not supported!
			logger.warning("Invalid Report Type. Report can't be imported!");
		}

		// reset upploads
		reportUploads = new ItemCollection();
	}
}
