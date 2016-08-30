package com.imixs.workflow.office.util;

import java.io.File;
import java.io.FileOutputStream;
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
import org.imixs.workflow.xml.DocumentCollection;
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

	public static String SETUP_PROPERTIES = "/setup.properties";

	public static List<ItemCollection> modelData = null;

	private static Properties bundle;
	private static Properties setup;
	private static List<String> fieldList = null;

	private final static Logger logger = Logger
			.getLogger(TranslateModelData.class.getName());

	@Before
	public void setup() {

		// read setup property file
		setup = new Properties();
		try {
			InputStream in = getClass().getResourceAsStream(SETUP_PROPERTIES);
			setup.load(in);
			in.close();
		} catch (IOException e1) {
			e1.printStackTrace();
			Assert.fail();
		}

		bundle = new Properties();
		try {
			String bundleFile = setup.getProperty("org.imixs.model.bundle");
			InputStream in = getClass().getResourceAsStream(bundleFile);
			bundle.load(in);
			in.close();
		} catch (IOException e1) {
			e1.printStackTrace();
			Assert.fail();
		}

		// read fieldlist
		fieldList = new ArrayList<String>();
		String sFields = setup.getProperty("org.imixs.model.fields");
		StringTokenizer stFields = new StringTokenizer(sFields, ",");
		while (stFields.hasMoreElements()) {
			fieldList.add(stFields.nextToken());
		}

		try {
			String sModelSource = setup.getProperty("org.imixs.model.source");
			modelData = XMLItemCollectionAdapter
					.readCollectionFromInputStream(getClass()
							.getResourceAsStream(sModelSource));
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

		Assert.assertTrue(modelData.size() > 1);
	}

	/**
	 * Writes a model.property file with all lables to be translated. This file
	 * can be used for translation
	 */
	@Test
	public void generateDefaultModelProperties() {

		Properties defaultBundle = new Properties();
		logger.info("generate Model Properties ");

		for (ItemCollection modelEntity : modelData) {
			// iterate over each field
			for (String aField : fieldList) {
				String sValue = modelEntity.getItemValueString(aField);
				if (!sValue.isEmpty()) {
					logger.fine("check field " + aField + "=" + sValue);
					// replace ' ' with '_'
					String sKeyValue = getKeyFor(sValue);
					// test if found in resource bundle
					String newValue = defaultBundle.getProperty(sKeyValue);

					if (newValue == null) {
						// create property
						defaultBundle.setProperty(sKeyValue, sValue);
					}
				} else {
					logger.fine("check field " + aField + "=null" + sValue);
				}
			}

		}
		// save properties to project root folder
		try {
			defaultBundle.store(new FileOutputStream(
					"src/test/resources/defaultBundle.properties"), null);
		} catch (Exception e) {

			e.printStackTrace();
			Assert.fail();
		}

		Assert.assertNotNull(defaultBundle);
	}

	/**
	 * Writes a new translated ixm file
	 */
	@Test
	public void translateModel() {
		// translate modelData
		modelData = translateModelData(modelData);

		// create JAXB object
		DocumentCollection xmlCol = null;
		try {
			xmlCol = XMLItemCollectionAdapter.putCollection(modelData);
		} catch (Exception e1) {

			e1.printStackTrace();
			Assert.fail();
		}

		// now write back to file
		File file = null;
		try {
			String sTargetFile = setup.getProperty("org.imixs.model.target");
			file = new File(sTargetFile);
			JAXBContext jaxbContext = JAXBContext
					.newInstance(DocumentCollection.class);
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
	static List<ItemCollection> translateModelData(List<ItemCollection> aModel) {

		List<ItemCollection> newModelData = new ArrayList<ItemCollection>();

		for (ItemCollection modelEntity : aModel) {

			// iterate over each field
			for (String aField : fieldList) {

				String sValue = modelEntity.getItemValueString(aField);

				logger.info("Translater translate " + aField + ":" + sValue);

				// replace ' ' with '_'
				sValue = getKeyFor(sValue);

				// test if found in resource bundle
				String newValue = bundle.getProperty(sValue);

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

	/**
	 * Genereate a unique property key
	 * 
	 * @param key
	 * @return
	 */
	static String getKeyFor(String key) {
		key = key.replace(' ', '_');
		key = key.replace("ä", "ae");
		key = key.replace("ö", "oe");
		key = key.replace("ü", "ue");
		key = key.replace("ß", "ss");
		key = key.replace("Ä", "Ae");
		key = key.replace("Ö", "Oe");
		key = key.replace("Ü", "Ue");

		return key;

	}

}
