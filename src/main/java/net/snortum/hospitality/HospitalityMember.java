package net.snortum.hospitality;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class defines a single member.
 * 
 * @author Knute Snortum, (c) copyright 2011-2013
 * @version 1.0
 * 
 */
public class HospitalityMember {

	public final static String DATE_FORMAT = "MM/dd/yyyy";
	private final static SimpleDateFormat FORMATTER =
			new SimpleDateFormat( DATE_FORMAT );
	private static int lastMemberNumber = 1;

	// Fields
	private Integer memberNumber;
	private String firstName;
	private String lastName;
	private HospitalityMember scheduleWith = null;
	private boolean teamLeader;
	private Date lastScheduled = null;
	private boolean lookedAt = false;
	private String reason = "";

	// Getters/setters

	/**
	 * @return lastMemberNumber *
	 */
	public static int getLastMemberNumber() {
		return lastMemberNumber;
	}

	/**
	 * @param lastMemberNumber
	 */
	public static void setLastMemberNumber( int lastMemberNumber ) {
		HospitalityMember.lastMemberNumber = lastMemberNumber;
	}

	/**
	 * @return the member number
	 */
	public Integer getMemberNumber() {
		return memberNumber;
	}

	/**
	 * @param memberNumber
	 *            the member number to set
	 */
	public void setMemberNumber( Integer memberNumber ) {
		this.memberNumber = memberNumber;
	}

	/**
	 * Set the next member number
	 */
	public void nextMemberNumber() {
		this.memberNumber = new Integer( lastMemberNumber++ );
	}

	/**
	 * @return the first name
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName
	 *            the first name to set
	 */
	public void setFirstName( String firstName ) {
		this.firstName = firstName;
	}

	/**
	 * @return the last name
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName
	 *            the last name to set
	 */
	public void setLastName( String lastName ) {
		this.lastName = lastName;
	}

	/**
	 * @return the member number of the member to schedule with
	 */
	public HospitalityMember getScheduleWith() {
		return scheduleWith;
	}

	/**
	 * @param scheduleWith
	 *            the member number to schedule with
	 */
	public void setScheduleWith( HospitalityMember scheduleWith ) {
		this.scheduleWith = scheduleWith;
	}

	/**
	 * @return is this member a team leader
	 */
	public boolean isTeamLeader() {
		return teamLeader;
	}

	/**
	 * @param teamLeader
	 *            is this member a team leader
	 */
	public void setTeamLeader( boolean teamLeader ) {
		this.teamLeader = teamLeader;
	}

	/**
	 * @param the
	 *            date last scheduled
	 */
	public void setLastScheduled( Date lastScheduled ) {
		this.lastScheduled = lastScheduled;
	}

	/**
	 * @return lastScheduled the last scheduled date to set
	 */
	public Date getLastScheduled() {
		return lastScheduled;
	}

	/**
	 * @return has this member been looked at
	 */
	public boolean isLookedAt() {
		return lookedAt;
	}

	/**
	 * @param lookedAt
	 *            has this member been looked at
	 */
	public void setLookedAt( boolean lookedAt ) {
		this.lookedAt = lookedAt;
	}

	/**
	 * @return reason
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * @param reason
	 */
	public void setReason( String reason ) {
		this.reason = reason;
	}

	/**
	 * @return the last schedule date or "Never"
	 */
	public String displayLastScheduled() {
		return lastScheduled == null
				? FORMATTER.format( lastScheduled )
				: "Never";
	}

	/**
	 * @return first and last name
	 */
	public String displayFullName() {
		return getLastName() + ", " + getFirstName();
	}

	// Constructors

	/**
	 * Default constructor
	 */
	public HospitalityMember() {
	}

	/**
	 * Create a new member from form data.
	 * 
	 * @param firstName
	 * @param lastName
	 * @param scheduleWith
	 * @param teamLeader
	 */
	public HospitalityMember( String firstName, String lastName,
			HospitalityMember scheduleWith, boolean teamLeader ) {
		this.memberNumber = Integer.valueOf( lastMemberNumber++ );
		this.firstName = firstName;
		this.lastName = lastName;
		this.scheduleWith = scheduleWith;
		this.teamLeader = teamLeader;
	}

	/**
	 * Create an existing member from the file
	 * 
	 * @param number
	 * @param firstName
	 * @param lastName
	 * @param scheduleWith
	 * @param teamLeader
	 * @param lastScheduled
	 */
	public HospitalityMember( Integer number, String firstName,
			String lastName, HospitalityMember scheduleWith,
			boolean teamLeader, Date lastScheduled ) {
		this.memberNumber = number;
		this.firstName = firstName;
		this.lastName = lastName;
		this.scheduleWith = scheduleWith;
		this.teamLeader = teamLeader;
		this.lastScheduled = lastScheduled;
	}

	// Public methods

	/**
	 * Member to string for display
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append( "Name:           " + displayFullName() + "\n" );
		sb.append( "Number:         " + getMemberNumber() + "\n" );

		if ( getScheduleWith() != null ) {
			sb.append( "Schedule With:  " + getScheduleWith().displayFullName()
					+ "\n" );
		}

		sb.append( "Team Leader:    " + isTeamLeader() + "\n" );
		sb.append( "Last Scheduled: " + displayLastScheduled() + "\n" );
		sb.append( "========================================\n\n" );

		return sb.toString();
	}

	/**
	 * Member to string for file format
	 * 
	 * @return string used to write to file
	 */
	public String toFile() {
		StringBuffer sb = new StringBuffer();
		sb.append( getMemberNumber() + "\t" );
		sb.append( getFirstName() + "\t" );
		sb.append( getLastName() + "\t" );

		if ( getScheduleWith() != null ) {
			sb.append( getScheduleWith().getMemberNumber() + "\t" );
		}
		else {
			sb.append( "\t" );
		}

		sb.append( isTeamLeader() + "\t" );

		if ( displayLastScheduled() == "Never" ) {
			sb.append( "\n" );
		}
		else {
			sb.append( displayLastScheduled() + "\n" );
		}

		return sb.toString();
	}

	/**
	 * Member to string for Find function.
	 * 
	 * @return a string formatted for Find
	 */
	public String toFind() {
		return displayFullName() + ": " + getMemberNumber() + "\n";
	}

}
