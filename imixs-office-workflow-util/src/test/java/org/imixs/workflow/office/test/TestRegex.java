package org.imixs.workflow.office.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.imixs.workflow.exceptions.PluginException;
import org.junit.Test;

import junit.framework.Assert;

/**
 * Test regex from the ProfilePugin
 * 
 * @author rsoika
 */
public class TestRegex {
	Pattern pattern;
	Matcher matcher;
	
	

	
	@Test
	public void testLucenel() throws PluginException {

		String typePattern = "workitem|workitemarchive|workitemdeleted";
		String processIDPattern = "";

		String type = "workitem";
		String sPid = "3120";

		// test type pattern
		Assert.assertFalse(typePattern != null && !"".equals(typePattern) && !type.matches(typePattern));

		// test $processid pattern
		Assert.assertFalse(processIDPattern != null && !"".equals(processIDPattern) && !sPid.matches(processIDPattern));

	}

	/**
	 * Teste file patterns
	 * 
	 * @throws PluginException
	 */
	@Test
	public void testFilePDFRegex() throws PluginException {

		String sFilePattern = "([^\\s]+(\\.(?i)(jpg|png|gif|bmp))$)";
		// String aFileName = "Workflow tool CR - include in migration to new
		// platform.png";

		Pattern pattern = Pattern.compile(sFilePattern);

		Assert.assertTrue(pattern.matcher("Workflow tool CR - include in migration to new platform.png").find());

		Assert.assertFalse(pattern.matcher("Workflow tool CR - include in migration to new platform.pdf").find());

		Assert.assertFalse(pattern.matcher("Workflow").find());

	}
}
