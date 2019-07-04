package org.imixs.workflow.office.test;

import org.junit.Assert;
import org.junit.Test;

import com.imixs.workflow.office.forms.DMSController;

public class DMSControllerTest {

	/**
	 * Test regex...
	 */
	@Test
	public void test() {
		String filename;

		filename = "http://www.imixs.org";
		Assert.assertTrue(filename.matches(DMSController.REGEX_URL_PATTERN));

		filename = "https://www.imixs.org";
		Assert.assertTrue(filename.matches(DMSController.REGEX_URL_PATTERN));

		filename = "file://test.txt";
		Assert.assertTrue(filename.matches(DMSController.REGEX_URL_PATTERN));

	}

}
