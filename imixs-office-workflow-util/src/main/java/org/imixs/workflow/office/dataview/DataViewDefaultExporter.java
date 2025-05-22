package org.imixs.workflow.office.dataview;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.imixs.workflow.FileData;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.office.forms.AnalyticController;

import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * The DataViewDefaultExporter exports a dataset into a excel template. The
 * exporter sends a DataViewExportEvent. An observer CID bean can implement
 * alternative exporters.
 * With the event property 'completed' a client can signal that the export
 * process is completed. Otherwise the default behavior will be adapted.
 * 
 * @author rsoika
 *
 */
@Named
@ConversationScoped
public class DataViewDefaultExporter implements Serializable {
    public static final String ERROR_CONFIG = "CONFIG_ERROR";

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(AnalyticController.class.getName());

    @Inject
    protected Event<DataViewExportEvent> dataViewExportEvents;

    /**
     * This helper method inserts a row for each invoice
     *
     * @throws PluginException
     */
    public void insertDataRows(List<ItemCollection> dataset, ItemCollection dataViewDefinition,
            List<ItemCollection> viewItemDefinitions,
            FileData fileData) throws PluginException {
        // load XSSFWorkbook
        try (InputStream imputStream = new ByteArrayInputStream(fileData.getContent())) {
            XSSFWorkbook doc = new XSSFWorkbook(imputStream);

            // send DataViewExportEvent....
            if (dataViewExportEvents != null) {
                DataViewExportEvent event = new DataViewExportEvent(dataset, dataViewDefinition, viewItemDefinitions,
                        doc);
                dataViewExportEvents.fire(event); // found FileData?
                if (!event.isCompleted()) {
                    // Default behavior
                    defaultExport(dataset, dataViewDefinition, viewItemDefinitions, doc);
                }
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            // write back the file
            doc.write(byteArrayOutputStream);
            doc.close();
            byte[] newContent = byteArrayOutputStream.toByteArray();
            fileData.setContent(newContent);

        } catch (IOException e) {
            throw new PluginException(DataViewPOIHelper.class.getSimpleName(), ERROR_CONFIG,
                    "failed to update excel export: " + e.getMessage());
        }
    }

    /**
     * This is the default implementation to insert a dataset into a XSSFSheet
     * 
     * @param dataset
     * @param referenceCell
     * @param viewItemDefinitions
     * @param doc
     */
    private void defaultExport(List<ItemCollection> dataset, ItemCollection dataViewDefinition,
            List<ItemCollection> viewItemDefinitions, XSSFWorkbook doc) {
        String referenceCell = dataViewDefinition.getItemValueString("poi.referenceCell");

        // NOTE: we only take the first sheet !
        XSSFSheet sheet = doc.getSheetAt(0);

        CellReference cr = new CellReference(referenceCell);
        XSSFRow referenceRow = sheet.getRow(cr.getRow());
        int referenceRowPos = referenceRow.getRowNum() + 1;
        int rowPos = referenceRowPos;
        // int lastRow = sheet.getLastRowNum();
        int lastRow = 999;
        logger.finest("Last rownum=" + lastRow);
        sheet.shiftRows(rowPos, lastRow, dataset.size(), true, true);

        for (ItemCollection workitem : dataset) {
            logger.finest("......copy row...");

            // now create a new line..
            XSSFRow row = sheet.createRow(rowPos);
            row.copyRowFrom(referenceRow, new CellCopyPolicy());
            // insert values
            int cellNum = 0;
            for (ItemCollection itemDef : viewItemDefinitions) {
                String type = itemDef.getItemValueString("item.type");
                String name = itemDef.getItemValueString("item.name");
                switch (type) {
                    case "xs:double":
                        row.getCell(cellNum).setCellValue(workitem.getItemValueDouble(name));
                        break;
                    case "xs:float":
                        row.getCell(cellNum).setCellValue(workitem.getItemValueFloat(name));
                        break;
                    case "xs:int":
                        row.getCell(cellNum).setCellValue(workitem.getItemValueInteger(name));
                        break;
                    case "xs:date":
                        row.getCell(cellNum).setCellValue(workitem.getItemValueDate(name));
                        break;

                    default:
                        row.getCell(cellNum).setCellValue(workitem.getItemValueString(name));
                }
                cellNum++;
            }

            rowPos++;
        }
        // delete reference row
        sheet.shiftRows(referenceRowPos, lastRow + dataset.size(), -1, true, true);

    }

}
