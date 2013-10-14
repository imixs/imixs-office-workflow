package com.imixs.workflow.office.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import junit.framework.Assert;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.xml.EntityCollection;
import org.imixs.workflow.xml.XMLItemCollectionAdapter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class translates models into a new language. The translation can be
 * started by JUnit Test run.
 * 
 * The source model file is specified by the field MODEL_SOURCE_FILE The target
 * model file is specified by the field MODEL_TARGET_FILE
 * 
 * The file "model_.properties" contains the setup and the resources to be
 * translated. The property "org.imixs.model.fields holds" a list of fields
 * where the values will be translated into the target language.
 * 
 * 
 * @author rsoika
 */
public class TranslateModelData {

	public static String MODEL_SOURCE_FILE = "/office-de-2.0.5.ixm";
	public static String MODEL_TARGET_FILE = "src/test/resources/generated-model.ixm";
	public static String MODEL_PROPERTIES = "/model_en.properties";

	public static List<ItemCollection> modelData = null;

	private static Properties properties;
	private static List<String> fieldList = null;

	private final static Logger logger = Logger
			.getLogger(TranslateModelData.class.getName());

	@Before
	public void setup() {

		properties = new Properties();
		try {
			InputStream in = getClass().getResourceAsStream(MODEL_PROPERTIES);
			properties.load(in);
			in.close();
		} catch (IOException e1) {
			e1.printStackTrace();
			Assert.fail();
		}

		// read fieldlist
		fieldList = new ArrayList<String>();
		String sFields = properties.getProperty("org.imixs.model.fields");
		StringTokenizer stFields = new StringTokenizer(sFields, ",");
		while (stFields.hasMoreElements()) {
			fieldList.add(stFields.nextToken());
		}

		try {
			modelData = XMLItemCollectionAdapter
					.readCollectionFromInputStream(getClass()
							.getResourceAsStream(MODEL_SOURCE_FILE));
		} catch (JAXBException e) {
			Assert.fail();
		} catch (IOException e) {
			Assert.fail();
		}

	}

	@After
	public void teardown() {

	}

	@Test
	public void testRead() {

		Assert.assertEquals(137, modelData.size());
	}

	/**
	 * Writes a new ixm file
	 */
	@Test
	public void generateModel() {

		// translate modelData
		modelData = translateModelData(modelData);

		// create JAXB object
		EntityCollection xmlCol = null;
		try {
			xmlCol = XMLItemCollectionAdapter.putCollection(modelData);
		} catch (Exception e1) {

			e1.printStackTrace();
			Assert.fail();
		}

		// now write back to file
		File file = null;
		try {

			file = new File(MODEL_TARGET_FILE);
			JAXBContext jaxbContext = JAXBContext
					.newInstance(EntityCollection.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			// output pretty printed
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			jaxbMarshaller.marshal(xmlCol, file);
			// jaxbMarshaller.marshal(xmlCol, System.out);

		} catch (JAXBException e) {
			e.printStackTrace();
			Assert.fail();
		}
		Assert.assertNotNull(file);
	}

	/**
	 * This Method translates the model entity collection
	 * 
	 * @param model
	 * @return
	 */
	private static List<ItemCollection> translateModelData(
			List<ItemCollection> aModel) {

		List<ItemCollection> newModelData = new ArrayList<ItemCollection>();

		for (ItemCollection modelEntity : aModel) {

			// iterate over each field
			for (String aField : fieldList) {

				String sValue = modelEntity.getItemValueString(aField);

				logger.info("Translater translate " + aField + ":" + sValue);

				// test if found in resource bundle
				String newValue = properties.getProperty(sValue);
				if (newValue != null && !newValue.isEmpty()) {
					logger.info("Translater found : " + sValue + " -> "
							+ newValue);
					modelEntity.replaceItemValue(aField, newValue);
				}

			}

			newModelData.add(modelEntity);

		}
		return newModelData;

	}
}
