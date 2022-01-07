package org.imixs.workflow.office.test;

import java.util.Calendar;

import org.imixs.workflow.office.util.SequenceService;
import org.imixs.workflow.office.util.SequenceService.SequenceNumber;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test different sequence number formats
 * 
 * @author rsoika
 */
public class SequenceNumberTest {
	SequenceService sequenceService = null;

	@Before
	public void setUp() throws Exception {
		sequenceService = new SequenceService();
	}

	@Test
	public void testDigitsSimple() {
		// test simple
		SequenceNumber seqn = sequenceService.new SequenceNumber("2022001");
		Assert.assertEquals("2022001", seqn.getDigit());
		Assert.assertEquals("2022002", seqn.getNextDigit());
		Assert.assertEquals("", seqn.getPrafix());

		Assert.assertEquals("2022001", seqn.getNextSequenceNumber());
		Assert.assertEquals("2022002", seqn.getNextDev());
	}

	@Test
	public void testDigitsPraefix() {
		// test without leading 0
		SequenceNumber seqn = sequenceService.new SequenceNumber("R<date>YYYY</date>00001");
		Assert.assertEquals("00001", seqn.getDigit());
		Assert.assertEquals("00002", seqn.getNextDigit());
		Assert.assertEquals("R<date>YYYY</date>", seqn.getPrafix());

		Calendar cal = Calendar.getInstance();
		Assert.assertEquals("R" + cal.get(Calendar.YEAR) + "00001", seqn.getNextSequenceNumber());
		Assert.assertEquals("R<date>YYYY</date>00002", seqn.getNextDev());
	}

	@Test
	public void testDigitsLeadingZero() {

		// test with prafix and leading 0
		SequenceNumber seqn = sequenceService.new SequenceNumber("000001");
		Assert.assertEquals("000001", seqn.getDigit());
		Assert.assertEquals("000002", seqn.getNextDigit());
		Assert.assertEquals("", seqn.getPrafix());

		Assert.assertEquals("000001", seqn.getNextSequenceNumber());
		Assert.assertEquals("000002", seqn.getNextDev());

		// test with prafix and leading 0
		seqn = sequenceService.new SequenceNumber("R000001");
		Assert.assertEquals("000001", seqn.getDigit());
		Assert.assertEquals("000002", seqn.getNextDigit());
		Assert.assertEquals("R", seqn.getPrafix());

		Assert.assertEquals("R000001", seqn.getNextSequenceNumber());
		Assert.assertEquals("R000002", seqn.getNextDev());
	}

}
