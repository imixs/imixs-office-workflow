package org.imixs.workflow.office.test;

import org.imixs.workflow.office.forms.DMSController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DMSControllerTest {

	/**
	 * Test regex...
	 */
	@Test
	public void test() {
		String filename;

		filename = "http://www.imixs.org";
		Assertions.assertTrue(filename.matches(DMSController.REGEX_URL_PATTERN));

		filename = "https://www.imixs.org";
		Assertions.assertTrue(filename.matches(DMSController.REGEX_URL_PATTERN));

		filename = "file://test.txt";
		Assertions.assertTrue(filename.matches(DMSController.REGEX_URL_PATTERN));

	}

}
