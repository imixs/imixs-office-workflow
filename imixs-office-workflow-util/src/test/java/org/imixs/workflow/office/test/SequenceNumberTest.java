package org.imixs.workflow.office.test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Calendar;
import java.util.List;

import org.imixs.workflow.office.util.SequenceService;
import org.imixs.workflow.office.util.SequenceService.SequenceNumber;
import org.imixs.workflow.util.XMLParser;
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

	@Test
	public void testSpecificDate_29Dec2025() throws Exception {
		// Test mit festem Datum 29.12.2025
		LocalDate date = LocalDate.of(2025, 12, 29);

		String nextSequenceNumber = "20<date>YY</date>10547";

		List<String> dateTags = XMLParser.findTags(nextSequenceNumber, "date");
		for (String tag : dateTags) {
			String pattern = XMLParser.findTagValue(tag, "date");

			// Map legacy patterns to java.time patterns
			String javaTimePattern = SequenceNumber.mapDatePattern(pattern);

			DateTimeFormatter formatter = new DateTimeFormatterBuilder()
					.appendPattern(javaTimePattern)
					.parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
					.toFormatter();

			String dateValue = date.format(formatter);
			nextSequenceNumber = nextSequenceNumber.replace(tag, dateValue);
		}

		System.out.println("Result=" + nextSequenceNumber);

		Assert.assertEquals("202510547", nextSequenceNumber);
	}

	@Test
	public void testSpecificDate_29Dec2025Double() throws Exception {
		// Test mit festem Datum 29.12.2025
		LocalDate date = LocalDate.of(2025, 12, 29);

		String nextSequenceNumber = "<date>YYYY</date>10547";

		List<String> dateTags = XMLParser.findTags(nextSequenceNumber, "date");
		for (String tag : dateTags) {
			String pattern = XMLParser.findTagValue(tag, "date");

			// Map legacy patterns to java.time patterns
			String javaTimePattern = SequenceNumber.mapDatePattern(pattern);

			DateTimeFormatter formatter = new DateTimeFormatterBuilder()
					.appendPattern(javaTimePattern)
					.parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
					.toFormatter();

			String dateValue = date.format(formatter);
			nextSequenceNumber = nextSequenceNumber.replace(tag, dateValue);
		}

		System.out.println("Result=" + nextSequenceNumber);

		Assert.assertEquals("202510547", nextSequenceNumber);
	}

	// =========== new tests for date formating ===========

	@Test
	public void testDateFormatYY() {
		// Test mit "YY" (zwei letzte Ziffern des Jahres)
		String pattern = "30<date>YY</date>10547";
		SequenceNumber seqn = sequenceService.new SequenceNumber(pattern);

		String currentYearLastTwoDigits = String.format("%02d", LocalDate.now().getYear() % 100);
		String expected = "30" + currentYearLastTwoDigits + "10547";
		System.out.println("Result=" + expected);
		Assert.assertEquals(expected, seqn.getNextSequenceNumber());
	}

	@Test
	public void testDateFormatYYYY() {
		// Test mit "YYYY" (vierstelliges Jahr)
		String pattern = "R<date>YYYY</date>001";
		SequenceNumber seqn = sequenceService.new SequenceNumber(pattern);

		int currentYear = LocalDate.now().getYear();
		String expected = "R" + currentYear + "001";
		System.out.println("Result=" + expected);
		Assert.assertEquals(expected, seqn.getNextSequenceNumber());
	}

	@Test
	public void testDateFormatMM() {
		// Test mit Monat
		String pattern = "INV<date>YYYYMM</date>001";
		SequenceNumber seqn = sequenceService.new SequenceNumber(pattern);

		String currentYearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
		String expected = "INV" + currentYearMonth + "001";
		System.out.println("Result=" + expected);
		Assert.assertEquals(expected, seqn.getNextSequenceNumber());
	}

	@Test
	public void testDateFormatDDMMYYYY() {
		// Test mit komplettem Datum
		String pattern = "DOC<date>DDMMYYYY</date>001";
		SequenceNumber seqn = sequenceService.new SequenceNumber(pattern);

		String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
		String expected = "DOC" + currentDate + "001";
		System.out.println("Result=" + expected);
		Assert.assertEquals(expected, seqn.getNextSequenceNumber());
	}

	@Test
	public void testMultipleDateTags() {
		// Test mit mehreren Datums-Tags
		String pattern = "<date>YY</date>-<date>MM</date>-<date>DD</date>-001";
		SequenceNumber seqn = sequenceService.new SequenceNumber(pattern);

		LocalDate now = LocalDate.now();
		String yy = String.format("%02d", now.getYear() % 100);
		String mm = String.format("%02d", now.getMonthValue());
		String dd = String.format("%02d", now.getDayOfMonth());

		String expected = yy + "-" + mm + "-" + dd + "-001";
		System.out.println("Result=" + expected);
		Assert.assertEquals(expected, seqn.getNextSequenceNumber());
	}

	@Test
	public void testMixedCasePatterns() {
		// Test mit unterschiedlichen Schreibweisen
		SequenceNumber seqn1 = sequenceService.new SequenceNumber("TEST<date>yy</date>001");
		SequenceNumber seqn2 = sequenceService.new SequenceNumber("TEST<date>YY</date>001");

		// Beide sollten das gleiche Ergebnis liefern (letzte 2 Ziffern)
		String expected = "TEST" + String.format("%02d", LocalDate.now().getYear() % 100) + "001";
		System.out.println("Result=" + expected);
		Assert.assertEquals(expected, seqn1.getNextSequenceNumber());
		Assert.assertEquals(expected, seqn2.getNextSequenceNumber());
	}

	@Test
	public void testComplexPattern() {
		// Komplexes Pattern mit verschiedenen Komponenten
		String pattern = "ORD-<date>YYYY</date>-<date>MM</date>-<date>DD</date>-0001";
		SequenceNumber seqn = sequenceService.new SequenceNumber(pattern);

		LocalDate now = LocalDate.now();
		String expected = String.format("ORD-%04d-%02d-%02d-0001",
				now.getYear(),
				now.getMonthValue(),
				now.getDayOfMonth());

		Assert.assertEquals(expected, seqn.getNextSequenceNumber());
	}

	@Test
	public void testNoDateTag() {
		// Test ohne Datums-Tag
		SequenceNumber seqn = sequenceService.new SequenceNumber("FIXED-0001");

		Assert.assertEquals("0001", seqn.getDigit());
		Assert.assertEquals("0002", seqn.getNextDigit());
		Assert.assertEquals("FIXED-", seqn.getPrafix());
		Assert.assertEquals("FIXED-0001", seqn.getNextSequenceNumber());
		Assert.assertEquals("FIXED-0002", seqn.getNextDev());
	}

	@Test
	public void testDateTagWithSpecialChars() {
		// Test mit Sonderzeichen im Pattern
		String pattern = "INV_<date>YY</date>_<date>MM</date>_001";
		SequenceNumber seqn = sequenceService.new SequenceNumber(pattern);

		LocalDate now = LocalDate.now();
		String yy = String.format("%02d", now.getYear() % 100);
		String mm = String.format("%02d", now.getMonthValue());

		String expected = "INV_" + yy + "_" + mm + "_001";

		Assert.assertEquals(expected, seqn.getNextSequenceNumber());
	}

	// =========== GRENZFALL-TESTS ===========

	@Test
	public void testYearBoundaryYY() {
		// Test für Jahreswechsel mit YY Format
		// Simuliere Ende Dezember 2025 -> "25" (nicht "26" wie bei Week Year!)
		// Dieser Test erfordert Mocking des aktuellen Datums
		// Hier nur als Beispiel-Struktur:
		String pattern = "20<date>YY</date>001";
		SequenceNumber seqn = sequenceService.new SequenceNumber(pattern);

		// Erwartung: Immer die letzten 2 Ziffern des Kalenderjahres
		String result = seqn.getNextSequenceNumber();
		Assert.assertTrue("Ergebnis sollte mit '20' beginnen", result.startsWith("20"));

		// Extrahiere die Jahresziffern
		String yearDigits = result.substring(2, 4);
		int currentYearLastTwo = LocalDate.now().getYear() % 100;
		Assert.assertEquals(String.format("%02d", currentYearLastTwo), yearDigits);
	}

	@Test
	public void testMonthFormatSingleDigit() {
		// Test mit einstelligem Monat (M vs MM)
		String pattern = "TEST<date>YYYY-M-D</date>001";
		SequenceNumber seqn = sequenceService.new SequenceNumber(pattern);

		LocalDate now = LocalDate.now();
		String expected = String.format("TEST%d-%d-%d001",
				now.getYear(),
				now.getMonthValue(),
				now.getDayOfMonth());

		Assert.assertEquals(expected, seqn.getNextSequenceNumber());
	}

	@Test
	public void testEmptyPattern() {
		// Test mit leerem Pattern im date-Tag
		String pattern = "TEST<date></date>001";
		SequenceNumber seqn = sequenceService.new SequenceNumber(pattern);

		// Erwartung: Leeres Tag wird durch leeren String ersetzt
		Assert.assertEquals("TEST001", seqn.getNextSequenceNumber());
	}

	@Test
	public void testVeryLongNumber() {
		// Test mit sehr langer Zahl
		String pattern = "ORD-<date>YY</date>-00000000000000000001";
		SequenceNumber seqn = sequenceService.new SequenceNumber(pattern);

		String yearDigits = String.format("%02d", LocalDate.now().getYear() % 100);
		String expected = "ORD-" + yearDigits + "-00000000000000000001";

		Assert.assertEquals(expected, seqn.getNextSequenceNumber());
		Assert.assertEquals("00000000000000000001", seqn.getDigit());
		Assert.assertEquals("00000000000000000002", seqn.getNextDigit());
	}

}
