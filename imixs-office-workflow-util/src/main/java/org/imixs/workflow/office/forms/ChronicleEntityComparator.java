package org.imixs.workflow.office.forms;

import java.text.Collator;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * Compares two ChronicleEntities by its date value.
 * 
 * @author rsoika
 * 
 */
public class ChronicleEntityComparator implements Comparator<ChronicleEntity> {
	@SuppressWarnings("unused")
	private final Collator collator;
	private final boolean ascending;

	public ChronicleEntityComparator(boolean ascending, Locale locale) {
		this.collator = Collator.getInstance(locale);
		this.ascending = ascending;
	}

	/**
	 * This method sorts by the default locale
	 * 
	 * @param aItemName
	 * @param ascending
	 */
	public ChronicleEntityComparator(boolean ascending) {
		this.collator = Collator.getInstance(Locale.getDefault());
		this.ascending = ascending;

	}

	/**
	 * This method sorts by the default locale ascending
	 * 
	 * @param aItemName
	 * @param ascending
	 */
	public ChronicleEntityComparator() {
		this.collator = Collator.getInstance(Locale.getDefault());
		this.ascending = true;

	}

	public int compare(ChronicleEntity a, ChronicleEntity b) {

		Date dateA = a.getDate();
		Date dateB = b.getDate();
		if (dateA == null && dateB != null) {
			return 1;
		}
		if (dateB == null && dateA != null) {
			return -1;
		}
		if (dateB == null && dateA == null) {
			return 0;
		}

		int result = dateB.compareTo(dateA);
		if (!this.ascending) {
			result = -result;
		}
		return result;

	}

}
