package net.snortum.hospitality;

import java.awt.Dimension;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 * <p>
 * The Scheduler window runs automatically, using the Hospitility.txt, and acts
 * as if you've pressed the Select button (explained below). Five members are
 * selected and the reasons they were are printed next to their names and member
 * numbers. The Selected check boxes can be cleared meaning that this particular
 * member shouldn't be scheduled. The fields will clear for that member.
 * </p>
 * 
 * <p>
 * The Schedule Update Date is the date that will be written back to the members
 * as the last scheduled date. Members are mainly scheduled based on the oldest
 * last scheduled date. If you have unchecked a Selected box, you can press the
 * Select button to have the program select new members. Once you have your five
 * members selected the way you want, write down the names and press the
 * Schedule button. This updates the Last Scheduled Date and clears the form.
 * You can press Select again if you want to schedule five more members.
 * </p>
 * 
 * @author Knute Snortum, (c) copyright 2011-2013
 * @version 1.1
 */
public class Schedule {

	// GUI globals
	private JFrame frame;
	private JPanel contentPane;
	private JLabel labelMember1, labelMember2, labelMember3, labelMember4,
			labelMember5;
	private JLabel labelMemberNumber1, labelMemberNumber2, labelMemberNumber3,
			labelMemberNumber4, labelMemberNumber5;
	private JLabel labelReason1, labelReason2, labelReason3, labelReason4,
			labelReason5;
	private JCheckBox checkSelected1, checkSelected2, checkSelected3,
			checkSelected4, checkSelected5;
	private JFormattedTextField textUpdateDate;

	// Data globals
	private HospitalityMember[] hma;
	private HashMap<Integer, HospitalityMember> hmMap;
	private ArrayList<HospitalityMember> selectedMembers;
	private TreeMap<Integer, HospitalityMember> tree;

	/**
	 * Main - run the GUI.
	 * 
	 * @param args
	 *            - not used
	 */
	public static void main( String[] args ) {
		Schedule gui = new Schedule();
		gui.start();
	}

	/**
	 * Create the GUI frame, menus, open file, load records, select and display
	 * members
	 */
	private void start() {
		frame = new JFrame( "Schedule Hospitality Members" );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.setLocation( 100, 100 );
		contentPane = (JPanel) frame.getContentPane();
		Utility.installLookAndFeel();

		makeMenus();
		makeContent();
		HospitalityMembersFile.openFile( frame );
		tree = HospitalityMembersFile.loadRecords( frame );
		sortRecords();
		selectedMembers = new ArrayList<HospitalityMember>();
		selectMembers();
		displayMembers();

		frame.pack();
		frame.setVisible( true );
	}

	/**
	 * Make the menus, helper method for {@link #start()	 */
	private void makeMenus() {
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar( menuBar );
		menuBar.add( makeFileMenu() );
		menuBar.add( makeHelpMenu() );
	}

	/** 
	 * Make the File menu, helper method for {@link #makeMenus()
	 * 
	 * @return the file menu to add
	 */
	private JMenu makeFileMenu() {
		JMenu menu;
		JMenuItem menuItem;

		// set up the File menu
		menu = new JMenu( "File" );
		menu.setMnemonic( KeyEvent.VK_F );

		// add Select menu item
		menuItem = new JMenuItem( "Select" );
		menuItem.setMnemonic( KeyEvent.VK_S );
		menuItem.addActionListener( new SelectListener() );
		menuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_S,
				Event.CTRL_MASK ) );
		menu.add( menuItem );

		// add Schedule
		menuItem = new JMenuItem( "Schedule" );
		menuItem.setMnemonic( KeyEvent.VK_C );
		menuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_C,
				Event.CTRL_MASK ) );
		menuItem.addActionListener( new ScheduleListener() );
		menu.add( menuItem );

		// add Exit menu item
		menu.addSeparator();
		menuItem = new JMenuItem( "Exit" );
		menuItem.setMnemonic( KeyEvent.VK_X );
		menuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_Q,
				Event.CTRL_MASK ) );
		menuItem.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				System.exit( 0 );
			}
		} );
		menu.add( menuItem );

		return menu;
	}

	/**
	 * Make the help menu, helper method for {@link #makeMenus()}
	 * 
	 * @return the help menu
	 */
	private JMenu makeHelpMenu() {
		JMenu menu;
		JMenuItem menuItem;

		// set up the Help menu
		menu = new JMenu( "Help" );
		menu.setMnemonic( KeyEvent.VK_H );

		// add About menu item
		menuItem = new JMenuItem( "About" );
		menuItem.setMnemonic( KeyEvent.VK_A );
		menuItem.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				// formatter:off
				JOptionPane.showMessageDialog(
						frame,
						"Schedule Hospitality Members\n\n" +
								"Version 1.0\n\n" +
								"(c) Copyright Knute Snortum 2011-2013\n" +
								"All rights reserved\n\n" +
								"This program is intended for a single user\n" +
								"and a membership of less than 1000",
						"About Schedule Hospitality Members",
						JOptionPane.INFORMATION_MESSAGE );
				// formatter:on
			}
		} );
		menu.add( menuItem );

		return menu;
	}

	/**
	 * Make GUI content pane, helper method for {@link #start()}
	 */
	private void makeContent() {

		// main panel
		contentPane.setLayout( new BoxLayout( contentPane, BoxLayout.Y_AXIS ) );
		contentPane.setBorder( BorderFactory.createEmptyBorder( 6, 6, 6, 6 ) );

		// labels and fields panel
		JPanel panel = new JPanel();
		panel.setLayout( new BoxLayout( panel, BoxLayout.Y_AXIS ) );
		panel.setBorder( BorderFactory.createEmptyBorder( 6, 6, 6, 6 ) );

		// one label and field
		panel.add( makeMember1() );
		panel.add( makeMember2() );
		panel.add( makeMember3() );
		panel.add( makeMember4() );
		panel.add( makeMember5() );
		panel.add( makeUpdateDate() );

		// updateDate default
		textUpdateDate.setValue( new Date() );

		// add buttons
		panel.add( makeButtons() );

		// add to main panel
		contentPane.add( panel );
	}

	/**
	 * Make first member, helper method to {@link #makeContent()}
	 * 
	 * @return member panel
	 */
	private JPanel makeMember1() {
		JPanel panel = new JPanel();
		panel.setLayout( new FlowLayout( FlowLayout.RIGHT, 3, 3 ) );

		labelMember1 = new JLabel();
		labelMember1.setPreferredSize( new Dimension( 250, 20 ) );
		labelMember1.setBorder( BorderFactory.createEtchedBorder() );
		panel.add( labelMember1 );

		labelMemberNumber1 = new JLabel();
		labelMemberNumber1.setPreferredSize( new Dimension( 40, 20 ) );
		labelMemberNumber1.setBorder( BorderFactory.createEtchedBorder() );
		panel.add( labelMemberNumber1 );

		labelReason1 = new JLabel();
		labelReason1.setPreferredSize( new Dimension( 250, 20 ) );
		labelReason1.setBorder( BorderFactory.createEtchedBorder() );
		panel.add( labelReason1 );

		checkSelected1 = new JCheckBox( "Selected" );
		checkSelected1.setName( "checkSelected1" );
		checkSelected1.addActionListener( new SelectedListener() );
		panel.add( checkSelected1 );

		return panel;
	}

	/**
	 * Make second member, helper method to {@link #makeContent()}
	 * 
	 * @return member panel
	 */
	private JPanel makeMember2() {
		JPanel panel = new JPanel();
		panel.setLayout( new FlowLayout( FlowLayout.RIGHT, 3, 3 ) );

		labelMember2 = new JLabel();
		labelMember2.setPreferredSize( new Dimension( 250, 20 ) );
		labelMember2.setBorder( BorderFactory.createEtchedBorder() );
		panel.add( labelMember2 );

		labelMemberNumber2 = new JLabel();
		labelMemberNumber2.setPreferredSize( new Dimension( 40, 20 ) );
		labelMemberNumber2.setBorder( BorderFactory.createEtchedBorder() );
		panel.add( labelMemberNumber2 );

		labelReason2 = new JLabel();
		labelReason2.setPreferredSize( new Dimension( 250, 20 ) );
		labelReason2.setBorder( BorderFactory.createEtchedBorder() );
		panel.add( labelReason2 );

		checkSelected2 = new JCheckBox( "Selected" );
		checkSelected2.setName( "checkSelected2" );
		checkSelected2.addActionListener( new SelectedListener() );
		panel.add( checkSelected2 );

		return panel;
	}

	/**
	 * Make third member, helper method to {@link #makeContent()}
	 * 
	 * @return member panel
	 */
	private JPanel makeMember3() {
		JPanel panel = new JPanel();
		panel.setLayout( new FlowLayout( FlowLayout.RIGHT, 3, 3 ) );

		labelMember3 = new JLabel();
		labelMember3.setPreferredSize( new Dimension( 250, 20 ) );
		labelMember3.setBorder( BorderFactory.createEtchedBorder() );
		panel.add( labelMember3 );

		labelMemberNumber3 = new JLabel();
		labelMemberNumber3.setPreferredSize( new Dimension( 40, 20 ) );
		labelMemberNumber3.setBorder( BorderFactory.createEtchedBorder() );
		panel.add( labelMemberNumber3 );

		labelReason3 = new JLabel();
		labelReason3.setPreferredSize( new Dimension( 250, 20 ) );
		labelReason3.setBorder( BorderFactory.createEtchedBorder() );
		panel.add( labelReason3 );

		checkSelected3 = new JCheckBox( "Selected" );
		checkSelected3.setName( "checkSelected3" );
		checkSelected3.addActionListener( new SelectedListener() );
		panel.add( checkSelected3 );

		return panel;
	}

	/**
	 * Make fourth member, helper method to {@link #makeContent()}
	 * 
	 * @return member panel
	 */
	private JPanel makeMember4() {
		JPanel panel = new JPanel();
		panel.setLayout( new FlowLayout( FlowLayout.RIGHT, 3, 3 ) );

		labelMember4 = new JLabel();
		labelMember4.setPreferredSize( new Dimension( 250, 20 ) );
		labelMember4.setBorder( BorderFactory.createEtchedBorder() );
		panel.add( labelMember4 );

		labelMemberNumber4 = new JLabel();
		labelMemberNumber4.setPreferredSize( new Dimension( 40, 20 ) );
		labelMemberNumber4.setBorder( BorderFactory.createEtchedBorder() );
		panel.add( labelMemberNumber4 );

		labelReason4 = new JLabel();
		labelReason4.setPreferredSize( new Dimension( 250, 20 ) );
		labelReason4.setBorder( BorderFactory.createEtchedBorder() );
		panel.add( labelReason4 );

		checkSelected4 = new JCheckBox( "Selected" );
		checkSelected4.setName( "checkSelected4" );
		checkSelected4.addActionListener( new SelectedListener() );
		panel.add( checkSelected4 );

		return panel;
	}

	/**
	 * Make fifth member, helper method to {@link #makeContent()}
	 * 
	 * @return member panel
	 */
	private JPanel makeMember5() {
		JPanel panel = new JPanel();
		panel.setLayout( new FlowLayout( FlowLayout.RIGHT, 3, 3 ) );

		labelMember5 = new JLabel();
		labelMember5.setPreferredSize( new Dimension( 250, 20 ) );
		labelMember5.setBorder( BorderFactory.createEtchedBorder() );
		panel.add( labelMember5 );

		labelMemberNumber5 = new JLabel();
		labelMemberNumber5.setPreferredSize( new Dimension( 40, 20 ) );
		labelMemberNumber5.setBorder( BorderFactory.createEtchedBorder() );
		panel.add( labelMemberNumber5 );

		labelReason5 = new JLabel();
		labelReason5.setPreferredSize( new Dimension( 250, 20 ) );
		labelReason5.setBorder( BorderFactory.createEtchedBorder() );
		panel.add( labelReason5 );

		checkSelected5 = new JCheckBox( "Selected" );
		checkSelected5.setName( "checkSelected5" );
		checkSelected5.addActionListener( new SelectedListener() );
		panel.add( checkSelected5 );

		return panel;
	}

	/**
	 * Make the Select and Schedule buttons, helper method to
	 * {@link #makeContent()}
	 * 
	 * @return button panel
	 */
	private JPanel makeButtons() {
		JPanel panel = new JPanel();
		panel.setLayout( new FlowLayout( FlowLayout.RIGHT, 3, 3 ) );

		JButton buttonUpdate = new JButton( "Select" );
		buttonUpdate.addActionListener( new SelectListener() );
		panel.add( buttonUpdate );

		JButton buttonNew = new JButton( "Schedule" );
		buttonNew.addActionListener( new ScheduleListener() );
		panel.add( buttonNew );

		return panel;
	}

	/**
	 * Make the Update Date text panel, helper method to {@link #makeContent()}
	 * 
	 * @return update date panel
	 */
	private JPanel makeUpdateDate() {
		JPanel panel = new JPanel();
		panel.setLayout( new FlowLayout( FlowLayout.RIGHT, 3, 3 ) );

		JLabel label = new JLabel( "Schedule Update Date: " );
		panel.add( label );

		textUpdateDate =
				new JFormattedTextField( new SimpleDateFormat(
						HospitalityMember.DATE_FORMAT ) );
		textUpdateDate.setPreferredSize( new Dimension( 70, 20 ) );
		panel.add( textUpdateDate );

		return panel;
	}

	/**
	 * Sort members by date.
	 * 
	 * <ul>
	 * <li><code>hmal</code> = temp variable to create <code>hma</code></li>
	 * <li><code>hma</code> = sorted members</li>
	 * <li><code>hmMap</code> = keyed members to update selected boolean</li>
	 * </ul>
	 */
	private void sortRecords() {
		Collection<HospitalityMember> hmal = tree.values();
		hma = new HospitalityMember[hmal.size()];
		hma = hmal.toArray( hma );
		Arrays.sort( hma, new DateSort() );
		hmal = null;
		hmMap = new HashMap<Integer, HospitalityMember>();

		for ( HospitalityMember i : hma ) {
			hmMap.put( i.getMemberNumber(), i );
		}
	}

	/**
	 * Select from file menu. Select and display members.
	 */
	private class SelectListener implements ActionListener {

		@Override
		public void actionPerformed( ActionEvent arg0 ) {
			selectMembers();
			displayMembers();
		}

	}

	/**
	 * Select members, helper method for {@link SelectListener#actionPerformed}
	 */
	private void selectMembers() {
		int hmIdx = 0;
		boolean isTeamLeaderSelected = false;

		// Get five members but leave a space for the team leader
		while ( selectedMembers.size() < 5
				|| ( isTeamLeaderSelected == false && selectedMembers.size() < 4 ) ) {
			HospitalityMember nominee = hma[hmIdx++];

			if ( nominee.isLookedAt() ) {
				continue;
			}

			nominee.setLookedAt( true );

			if ( isTeamLeaderSelected && nominee.isTeamLeader() ) {
				continue;
			}

			boolean alreadySelected = false;

			for ( HospitalityMember hm : selectedMembers ) {
				if ( hm == nominee ) {
					alreadySelected = true;
					break;
				}
			}

			if ( alreadySelected ) {
				continue;
			}

			if ( nominee.isTeamLeader() ) {
				isTeamLeaderSelected = true;
			}

			// check for sched with members
			if ( nominee.getScheduleWith() == null ) {
				selectedMembers.add( nominee );
				nominee.setReason( "Oldest scheduled date" );
			}
			else {
				selectSchedMembers( isTeamLeaderSelected, nominee );
			}

			// any more members?
			if ( hmIdx >= hma.length ) {
				break;
			}
		}

		selectTeamLeader( hmIdx, isTeamLeaderSelected );
	}

	/**
	 * Select a team leader, helper for {@link #selectedMembers}.
	 * 
	 * @param hmIdx
	 *            - index to member array
	 * @param isTeamLeaderSelected
	 */
	private void selectTeamLeader( int hmIdx, boolean isTeamLeaderSelected ) {
		int saveHmIdx = hmIdx;

		while ( hmIdx < hma.length && isTeamLeaderSelected == false ) {
			if ( !hma[hmIdx].isLookedAt() ) {
				if ( hma[hmIdx].isTeamLeader() ) {
					selectedMembers.add( hma[hmIdx] );
					hma[hmIdx].setReason( "Added team leader" );
					hma[hmIdx].setLookedAt( true );
					isTeamLeaderSelected = true;
					break;
				}
			}

			hmIdx++;
		}

		// if no team leader found, add anyone
		if ( isTeamLeaderSelected == false ) {
			hmIdx = saveHmIdx;

			while ( hmIdx < hma.length ) {
				if ( !hma[hmIdx].isLookedAt() ) {
					if ( hma[hmIdx].getScheduleWith() == null ) {
						selectedMembers.add( hma[hmIdx] );
						hma[hmIdx].setReason( "No team leaders found" );
						hma[hmIdx].setLookedAt( true );
						break;
					}
				}

				hmIdx++;
			}
		}
	}

	/**
	 * Select schedule members, helper for {@link #selectedMembers}.
	 * 
	 * @param isTeamLeaderSelected
	 * @param nominee
	 */
	private void selectSchedMembers( boolean isTeamLeaderSelected,
			HospitalityMember nominee ) {
		while ( selectedMembers.size() < 4
				|| ( isTeamLeaderSelected == false && selectedMembers.size() < 3 ) ) {

			// add nominee
			selectedMembers.add( nominee );
			nominee.setReason( "Oldest scheduled date" );

			// should we add the sched with member?
			HospitalityMember schedWith = nominee.getScheduleWith();
			boolean moreToSchedule = true;
			boolean schedWithFound = false;

			// check selected members first
			do {
				for ( HospitalityMember j : selectedMembers )
					if ( schedWith.equals( j ) ) {
						schedWithFound = true;
						break;
					}

				if ( schedWithFound == true ) {
					break;
				}

				// add sched with member
				selectedMembers.add( schedWith );
				schedWith.setReason( "Schedule with "
						+ nominee.getMemberNumber().toString() );
				schedWith.setLookedAt( true );

				// Check this member for sched with
				if ( schedWith.getScheduleWith() == null ) {
					moreToSchedule = false;
					break;
				}

				schedWith = schedWith.getScheduleWith();
			}
			while ( selectedMembers.size() < 4
					|| ( isTeamLeaderSelected == false && selectedMembers
							.size() < 3 ) );

			if ( schedWithFound || !moreToSchedule ) {
				break;
			}
		}
	}

	/**
	 * Display selected members. Helper method for
	 * {@link SelectListener#actionPerformed}
	 */
	private void displayMembers() {
		Iterator<HospitalityMember> it = selectedMembers.iterator();
		HospitalityMember hm;

		// Member 1
		if ( it.hasNext() ) {
			hm = it.next();
			labelMember1.setText( hm.displayFullName() );
			labelMemberNumber1.setText( hm.getMemberNumber().toString() );
			labelReason1.setText( hm.getReason() );
			checkSelected1.setSelected( hm.isLookedAt() );
		}
		else {
			labelMember1.setText( "" );
			labelMemberNumber1.setText( "" );
			labelReason1.setText( "" );
			checkSelected1.setSelected( false );
		}

		// Member 2
		if ( it.hasNext() ) {
			hm = it.next();
			labelMember2.setText( hm.displayFullName() );
			labelMemberNumber2.setText( hm.getMemberNumber().toString() );
			labelReason2.setText( hm.getReason() );
			checkSelected2.setSelected( hm.isLookedAt() );
		}
		else {
			labelMember2.setText( "" );
			labelMemberNumber2.setText( "" );
			labelReason2.setText( "" );
			checkSelected2.setSelected( false );
		}

		// Member 3
		if ( it.hasNext() ) {
			hm = it.next();
			labelMember3.setText( hm.displayFullName() );
			labelMemberNumber3.setText( hm.getMemberNumber().toString() );
			labelReason3.setText( hm.getReason() );
			checkSelected3.setSelected( hm.isLookedAt() );
		}
		else {
			labelMember3.setText( "" );
			labelMemberNumber3.setText( "" );
			labelReason3.setText( "" );
			checkSelected3.setSelected( false );
		}

		// Member 4
		if ( it.hasNext() ) {
			hm = it.next();
			labelMember4.setText( hm.displayFullName() );
			labelMemberNumber4.setText( hm.getMemberNumber().toString() );
			labelReason4.setText( hm.getReason() );
			checkSelected4.setSelected( hm.isLookedAt() );
		}
		else {
			labelMember4.setText( "" );
			labelMemberNumber4.setText( "" );
			labelReason4.setText( "" );
			checkSelected4.setSelected( false );
		}

		// Member 5
		if ( it.hasNext() ) {
			hm = it.next();
			labelMember5.setText( hm.displayFullName() );
			labelMemberNumber5.setText( hm.getMemberNumber().toString() );
			labelReason5.setText( hm.getReason() );
			checkSelected5.setSelected( hm.isLookedAt() );
		}
		else {
			labelMember5.setText( "" );
			labelMemberNumber5.setText( "" );
			labelReason5.setText( "" );
			checkSelected5.setSelected( false );
		}
	}

	/**
	 * Select and remove a member.
	 */
	private class SelectedListener implements ActionListener {

		@Override
		public void actionPerformed( ActionEvent arg0 ) {
			JCheckBox thisCheckBox = (JCheckBox) arg0.getSource();
			String memberNumber;

			if ( thisCheckBox.getName() == "checkSelected1" ) {
				memberNumber = labelMemberNumber1.getText();
			}
			else if ( thisCheckBox.getName() == "checkSelected2" ) {
				memberNumber = labelMemberNumber2.getText();
			}
			else if ( thisCheckBox.getName() == "checkSelected3" ) {
				memberNumber = labelMemberNumber3.getText();
			}
			else if ( thisCheckBox.getName() == "checkSelected4" ) {
				memberNumber = labelMemberNumber4.getText();
			}
			else if ( thisCheckBox.getName() == "checkSelected5" ) {
				memberNumber = labelMemberNumber5.getText();
			}
			else {
				memberNumber = "Error";
			}

			if ( !memberNumber.isEmpty() ) {
				HospitalityMember hm =
						hmMap.get( new Integer( Integer.parseInt( memberNumber ) ) );

				if ( !thisCheckBox.isSelected() ) {
					for ( HospitalityMember i : selectedMembers ) {
						if ( i == hm ) {
							selectedMembers.remove( hm );
							break;
						}
					}
				}
			}

			displayMembers();
		}

	}

	/**
	 * Schedule all selected members and write back to file.
	 */
	private class ScheduleListener implements ActionListener {

		@Override
		public void actionPerformed( ActionEvent arg0 ) {

			// update date
			Date updateDate;
			SimpleDateFormat formatter =
					new SimpleDateFormat( HospitalityMember.DATE_FORMAT );

			try {
				updateDate = formatter.parse( textUpdateDate.getText() );
			}
			catch ( ParseException e ) {
				//@formatter:off
				JOptionPane.showMessageDialog( 
						frame, 
						"Please enter a valid update date", 
						"Date Parse Error",
						JOptionPane.ERROR_MESSAGE );
				//formatter:on
				return;
			}

			for ( HospitalityMember i : selectedMembers ) {
				i.setLastScheduled( updateDate );
			}

			// write back to file
			BufferedWriter bw;
			try {
				bw = new BufferedWriter( new FileWriter( HospitalityMembersFile.getFileName() ) );
			}
			catch ( IOException e1 ) {
				//@formatter:off
				JOptionPane.showMessageDialog( 
						frame, 
						"Cannot open file \"" + HospitalityMembersFile.getFileName() + "\" for writing", 
						"File Open Error",
						JOptionPane.ERROR_MESSAGE );
				//formatter:on
				return;
			}

			// TODO this is bad IO, turn selectedMembers into hash?
			for ( HospitalityMember myHM : tree.values() ) {

				// is this an updated record?
				for ( HospitalityMember i : selectedMembers ) {
					if ( myHM.getMemberNumber() == i.getMemberNumber() ) {
						myHM = i;
						break;
					}
				}

				try {
					bw.write( myHM.toFile() );
				}
				catch ( IOException e2 ) {
					e2.printStackTrace();
					System.exit( 1 );
				}
			}

			try {
				bw.close();
			}
			catch ( IOException e3 ) {
			}

			// clear screen
			selectedMembers = new ArrayList< HospitalityMember >();
			displayMembers();
		}

	}

}
