package net.snortum.hospitality;

import java.util.Comparator;
import java.util.Date;

/**
 * Sorts "last scheduled dates" in members.
 * 
 * @author Knute Snortum, (c) copyright 2011-2013
 * @version 1.0
 * 
 */
public class DateSort implements Comparator<HospitalityMember> {

	/**
	 * Compare one scheduled date to another.
	 * 
	 * @param arg0
	 *            - Hospitality member object, one
	 * @param arg1
	 *            - Hospitality member object, two
	 * @return -1 if date0 &lt; date1 or date0 == null, 1 if date0 &gt; date1 or
	 *         date1 == null, 0 if arg0==arg1 or both null
	 */
	@Override
	public int compare( HospitalityMember arg0, HospitalityMember arg1 ) {
		Date date0 = arg0.getLastScheduled();
		Date date1 = arg1.getLastScheduled();

		if ( date0 == null && date1 == null ) {
			return 0;
		}

		if ( date0 == null ) {
			return -1;
		}

		if ( date1 == null ) {
			return 1;
		}

		return date0.compareTo( date1 );
	}

}
