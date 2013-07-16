package net.snortum.hospitality;

import java.awt.Dimension;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;

/**
 * <b>Summary</b>
 * 
 * <p>
 * This is where the members of the organization are entered into the system.
 * The text file HospitatiltyMembers.txt is selected by default. It is a tab
 * separated file that can be pulled into Excel or your spreadsheet program of
 * choice. After you get all members into the file, you will use this program
 * less and less. Start the Scheduler to begin scheduling members.
 * </p>
 * 
 * <b>Member View</b>
 * 
 * <p>
 * The Member View tab contains the individual fields for a member. First and
 * last name can be entered. Next is the member number of the member this person
 * should be scheduled with, if any. The Last Scheduled field is updated
 * automatically but can be changed here if necessary. The Team Leader box can
 * be checked if this person will be responsibly for the rest of the team.
 * </p>
 * 
 * <b>Buttons</b>
 * 
 * <p>
 * Press the Update button when you to write the data shown to the file. The New
 * button clears out all fields so you can start entering a new member. The
 * Remove button will delete to member that is showing.
 * </p>
 * 
 * <b>Navigation</b>
 * 
 * <p>
 * You can of course use the tab key to navigate the fields. To get to the next
 * member record use Alt-DownArrow. To go to the previous record, use
 * Alt-UpArrow. If you know the member number, you can jump to it directly by
 * pressing Ctrl-G (for Goto) and entering the member number and pressing OK.
 * </p>
 * 
 * <b>Members View Tab</b>
 * 
 * <p>
 * The Members View tab contains a listing of all members. This can be copied
 * and put into Word or other word processing program (use Ctrl-A and Ctrl-C).
 * You can scroll through the list to get an idea of who has been entered.
 * </p>
 * 
 * <b>Find View Tab</b>
 * 
 * <p>
 * You can enter in a last name, a first name, or both. When the search button
 * is press you can see all matches. To edit a specific member, copy or remember
 * their number and press Ctrl-G to go to that member.
 * </p>
 * 
 * @author Knute Snortum, (c) copyright 2011-2013
 * @version 1.1
 * 
 */
public class HospitalityMembers {

	// Instance vars for GUI
	private JFrame frame;
	private JPanel contentPane;
	private JTabbedPane tabPane;

	private JLabel textNumber, textSchedWithName;
	private JTextField textFirstName, textLastName, textScheduleWith,
			textLastScheduled, textFindLastName, textFindFirstName;
	private JCheckBox checkTeamLeader;

	private JTextArea membersTextArea, findTextArea;

	// Other vars
	private TreeMap<Integer, HospitalityMember> tree;

	/**
	 * Main - Create Members GUI
	 * 
	 * @param args
	 *            - not used
	 */
	public static void main( String[] args ) {
		HospitalityMembers gui = new HospitalityMembers();
		gui.start();
	}

	/**
	 * Setup frame, make menus and content pane, load records, display members.
	 */
	private void start() {
		frame = new JFrame( "Hospitality Members" );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.setLocation( 100, 100 );
		contentPane = (JPanel) frame.getContentPane();
		Utility.installLookAndFeel();

		makeMenus();
		makeContent();
		HospitalityMembersFile.openFile( frame );
		tree = HospitalityMembersFile.loadRecords( frame );
		displayMembers();

		if ( tree.isEmpty() ) {
			clearAll();
		} else {
			displayMember( tree.get( tree.firstKey() ) );
		}

		frame.pack();
		frame.setVisible( true );
	}

	/**
	 * Make menus, helper method for {@link #start()}
	 */
	private void makeMenus() {
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar( menuBar );
		menuBar.add( makeFileMenu() );
		menuBar.add( makeViewMenu() );
		menuBar.add( makeHelpMenu() );
	}

	/**
	 * Make a file menus, helper method for {@link #makeMenus()}
	 * 
	 * @return the file menu
	 */
	private JMenu makeFileMenu() {
		JMenu menu;
		JMenuItem menuItem;

		// set up the File menu
		menu = new JMenu( "File" );
		menu.setMnemonic( KeyEvent.VK_F );

		// add Save menu item
		menuItem = new JMenuItem( "Save" );
		menuItem.setMnemonic( KeyEvent.VK_S );
		menuItem.addActionListener( new UpdateListener() );
		menuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_S,
				Event.CTRL_MASK ) );
		menu.add( menuItem );

		// add New menu item
		menuItem = new JMenuItem( "New" );
		menuItem.setMnemonic( KeyEvent.VK_N );
		menuItem.addActionListener( new NewListener() );
		menuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_N,
				Event.CTRL_MASK ) );
		menu.add( menuItem );

		// add Exit menu item
		menu.addSeparator();
		menuItem = new JMenuItem( "Exit" );
		menuItem.setMnemonic( KeyEvent.VK_X );
		menuItem.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				System.exit( 0 );
			}
		} );
		menuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_Q,
				Event.CTRL_MASK ) );
		menu.add( menuItem );

		return menu;
	}

	/**
	 * Make view menu, helper method for {@link #makeMenus()}
	 * 
	 * @return the view menu
	 */
	private JMenu makeViewMenu() {
		JMenu menu;
		JMenuItem menuItem;

		// set up the View menu
		menu = new JMenu( "View" );
		menu.setMnemonic( KeyEvent.VK_V );

		// add Next Member menu item
		menuItem = new JMenuItem( "Next Member" );
		menuItem.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				nextRecord();
			}
		} );
		menuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_DOWN,
				Event.ALT_MASK ) );
		menu.add( menuItem );

		// add Previous Member menu item
		menuItem = new JMenuItem( "Previous Member" );
		menuItem.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				previousRecord();
			}
		} );
		menuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_UP,
				Event.ALT_MASK ) );
		menu.add( menuItem );

		// add Member View
		menu.addSeparator();
		menuItem = new JMenuItem( "Member View" );
		menuItem.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				tabPane.setSelectedIndex( 0 );
			}
		} );
		menu.add( menuItem );

		// add Members View
		menuItem = new JMenuItem( "Members View" );
		menuItem.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				tabPane.setSelectedIndex( 1 );
			}
		} );
		menu.add( menuItem );

		// find a member
		menu.addSeparator();
		menuItem = new JMenuItem( "Find a Member" );
		menuItem.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				tabPane.setSelectedIndex( 2 );
			}
		} );
		menuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_F,
				Event.CTRL_MASK ) );
		menu.add( menuItem );

		// goto a member
		menuItem = new JMenuItem( "Goto a Member" );
		menuItem.addActionListener( new GotoMenuItemListener() );
		menuItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_G,
				Event.CTRL_MASK ) );
		menu.add( menuItem );

		return menu;
	}

	/**
	 * Make help menu, helper method for {@link #makeMenus()}
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
				//@formatter:off
				JOptionPane.showMessageDialog( 
						frame, 
						"Hospitality Members\n\n" + 
						"Version 1.0\n\n" +
						"(c) Copyright Knute Snortum 2011-2013\n" + 
						"All rights reserved\n\n" + 
						"This program is intended for\n" + 
						"memberships of less than 1000", 
						"About Hospitality Members", 
						JOptionPane.INFORMATION_MESSAGE );
				//@formatter:on
			}
		} );
		menu.add( menuItem );

		return menu;
	}

	/**
	 * Make the content pane, helper method for {@link #start()}
	 */
	private void makeContent() {
		// setup tabs and panels, member view
		// main panel
		contentPane.setLayout( new BoxLayout( contentPane, BoxLayout.Y_AXIS ) );
		contentPane.setBorder( BorderFactory.createEmptyBorder( 6, 6, 6, 6 ) );
		tabPane = new JTabbedPane();

		// member panel (labels and fields)
		JPanel memberPanel = new JPanel();
		memberPanel.setLayout( new BoxLayout( memberPanel, BoxLayout.Y_AXIS ) );
		memberPanel.setBorder( BorderFactory.createEmptyBorder( 6, 6, 6, 6 ) );

		// one label and field
		memberPanel.add( makeNumber() );
		memberPanel.add( makeFirstName() );
		memberPanel.add( makeLastName() );
		memberPanel.add( makeScheduleWith() );
		memberPanel.add( makeLastScheduled() );
		memberPanel.add( makeTeamLeader() );

		// add buttons
		memberPanel.add( makeButtons() );

		// add to tab 0
		tabPane.addTab( "Member View", memberPanel );

		// add members view to tab 1
		JPanel membersPanel = new JPanel();
		membersPanel
				.setLayout( new BoxLayout( membersPanel, BoxLayout.Y_AXIS ) );
		membersPanel.setBorder( BorderFactory.createEmptyBorder( 6, 6, 6, 6 ) );

		membersTextArea = new JTextArea( 10, 25 );
		membersTextArea.setFont( new Font( "Courier", Font.PLAIN, 12 ) );

		JScrollPane membersScrollPane = new JScrollPane( membersTextArea );
		membersScrollPane
				.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED );
		membersScrollPane
				.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		membersPanel.add( membersScrollPane );
		tabPane.addTab( "Members View", membersPanel );

		// add find view to tab 2
		JPanel findPanel = new JPanel();
		findPanel.setLayout( new BoxLayout( findPanel, BoxLayout.Y_AXIS ) );
		findPanel.setBorder( BorderFactory.createEmptyBorder( 6, 6, 6, 6 ) );

		findPanel.add( makeFindLastName() );
		findPanel.add( makeFindFirstName() );

		findTextArea = new JTextArea( 5, 25 );
		findTextArea.setFont( new Font( "Courier", Font.PLAIN, 12 ) );

		JScrollPane findScrollPane = new JScrollPane( findTextArea );
		findScrollPane
				.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED );
		findScrollPane
				.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		findPanel.add( findScrollPane );

		findPanel.add( makeFindButtons() );

		tabPane.addTab( "Find View", findPanel );

		// add to main panel
		contentPane.add( tabPane );
	}

	/**
	 * Make number text field, helper method to {@link #makeContent()}
	 * 
	 * @return number field panel
	 */
	private JPanel makeNumber() {
		JPanel panel = new JPanel();
		panel.setLayout( new FlowLayout( FlowLayout.RIGHT, 3, 3 ) );
		panel.add( new JLabel( "Number: " ) );

		textNumber = new JLabel();
		textNumber.setPreferredSize( new Dimension( 40, 20 ) );
		textNumber.setBorder( BorderFactory.createEtchedBorder() );
		panel.add( textNumber );

		JLabel emptyLabel = new JLabel();
		emptyLabel.setPreferredSize( new Dimension( 208, 20 ) );
		panel.add( emptyLabel );

		return panel;
	}

	/**
	 * Make first name field, helper method to {@link #makeContent()}
	 * 
	 * @return first name panel
	 */
	private JPanel makeFirstName() {
		JPanel panel = new JPanel();
		panel.setLayout( new FlowLayout( FlowLayout.RIGHT, 3, 3 ) );
		JLabel labelFirstname = new JLabel( "First Name: " );
		panel.add( labelFirstname );

		textFirstName = new JTextField();
		textFirstName.setPreferredSize( new Dimension( 250, 20 ) );
		panel.add( textFirstName );

		return panel;
	}

	/**
	 * Make last name field, helper method to {@link #makeContent()}
	 * 
	 * @return last name panel
	 */
	private JPanel makeLastName() {
		JPanel panel = new JPanel();
		panel.setLayout( new FlowLayout( FlowLayout.RIGHT, 3, 3 ) );
		panel.add( new JLabel( "Last Name: " ) );

		textLastName = new JTextField();
		textLastName.setPreferredSize( new Dimension( 250, 20 ) );
		panel.add( textLastName );

		return panel;
	}

	/**
	 * Make schedule with field, helper method to {@link #makeContent()}
	 * 
	 * @return schedule with panel
	 */
	private JPanel makeScheduleWith() {
		JPanel panel = new JPanel();
		panel.setLayout( new FlowLayout( FlowLayout.RIGHT, 3, 3 ) );
		panel.add( new JLabel( "Schedule With: " ) );

		textScheduleWith = new JTextField();
		textScheduleWith.setPreferredSize( new Dimension( 40, 20 ) );
		textScheduleWith.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				checkSchedWith();
			}
		} );
		panel.add( textScheduleWith );

		textSchedWithName = new JLabel();
		textSchedWithName.setPreferredSize( new Dimension( 208, 20 ) );
		textSchedWithName.setBorder( BorderFactory.createEtchedBorder() );
		panel.add( textSchedWithName );

		return panel;
	}

	/**
	 * Make last schedule with field, helper method to {@link #makeContent()}
	 * 
	 * @return last scheduled with panel
	 */
	private JPanel makeLastScheduled() {
		JPanel panel = new JPanel();
		panel.setLayout( new FlowLayout( FlowLayout.RIGHT, 3, 3 ) );
		panel.add( new JLabel( "Last Scheduled: " ) );

		textLastScheduled = new JTextField();
		textLastScheduled.setPreferredSize( new Dimension( 70, 20 ) );
		textLastScheduled.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				checkLastSched();
			}
		} );
		panel.add( textLastScheduled );

		JLabel emptyLabel = new JLabel();
		emptyLabel.setPreferredSize( new Dimension( 178, 20 ) );
		panel.add( emptyLabel );

		return panel;
	}

	/**
	 * Make team leader field, helper method to {@link #makeContent()}
	 * 
	 * @return team leader panel
	 */
	private JPanel makeTeamLeader() {
		JPanel panel = new JPanel();
		panel.setLayout( new FlowLayout( FlowLayout.RIGHT, 3, 3 ) );

		checkTeamLeader = new JCheckBox( "Team Leader" );
		checkTeamLeader.setPreferredSize( new Dimension( 254, 20 ) );
		panel.add( checkTeamLeader );

		return panel;
	}

	/**
	 * Make find last name field, helper method to {@link #makeContent()}
	 * 
	 * @return find last name panel
	 */
	private JPanel makeFindLastName() {
		JPanel panel = new JPanel();
		panel.setLayout( new FlowLayout( FlowLayout.RIGHT, 3, 3 ) );
		panel.add( new JLabel( "Last Name: " ) );

		textFindLastName = new JTextField();
		textFindLastName.setPreferredSize( new Dimension( 250, 20 ) );
		textFindLastName.setBorder( BorderFactory.createEtchedBorder() );
		panel.add( textFindLastName );

		return panel;
	}

	/**
	 * Make find first name field, helper method to {@link #makeContent()}
	 * 
	 * @return find first name panel
	 */
	private JPanel makeFindFirstName() {
		JPanel panel = new JPanel();
		panel.setLayout( new FlowLayout( FlowLayout.RIGHT, 3, 3 ) );
		panel.add( new JLabel( "First Name: " ) );

		textFindFirstName = new JTextField();
		textFindFirstName.setPreferredSize( new Dimension( 250, 20 ) );
		textFindFirstName.setBorder( BorderFactory.createEtchedBorder() );
		panel.add( textFindFirstName );

		return panel;
	}

	/**
	 * Make buttons, helper method to {@link #makeContent()}
	 * 
	 * @return buttons panel
	 */
	private JPanel makeButtons() {
		JPanel panel = new JPanel();
		panel.setLayout( new FlowLayout( FlowLayout.RIGHT, 3, 3 ) );

		JButton buttonUpdate = new JButton( "Update" );
		buttonUpdate.addActionListener( new UpdateListener() );
		panel.add( buttonUpdate );

		JButton buttonNew = new JButton( "New" );
		buttonNew.addActionListener( new NewListener() );
		panel.add( buttonNew );

		JButton buttonRemove = new JButton( "Remove" );
		buttonRemove.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				Integer number = Integer.parseInt( textNumber.getText() );

				if ( tree.containsKey( number ) ) {
					tree.remove( number );
					updateFile();
				}
			}
		} );
		panel.add( buttonRemove );

		return panel;
	}

	/**
	 * Make find buttons, helper method to {@link #makeContent()}
	 * 
	 * @return find buttons panel
	 */
	private JPanel makeFindButtons() {
		JPanel panel = new JPanel();
		panel.setLayout( new FlowLayout( FlowLayout.RIGHT, 3, 3 ) );

		JButton buttonSearch = new JButton( "Search" );
		buttonSearch.addActionListener( new SearchListener() );
		panel.add( buttonSearch );

		return panel;
	}

	/**
	 * Display a hospitality member.
	 * 
	 * @param hm
	 *            - member to display
	 */
	private void displayMember( HospitalityMember hm ) {
		textNumber.setText( hm.getMemberNumber().toString() );
		textFirstName.setText( hm.getFirstName() );
		textLastName.setText( hm.getLastName() );

		if ( hm.getScheduleWith() != null ) {
			textScheduleWith.setText( hm.getScheduleWith().getMemberNumber()
					.toString() );
			textSchedWithName.setText( hm.getScheduleWith().displayFullName() );
		}
		else {
			textScheduleWith.setText( "" );
			textSchedWithName.setText( "" );
		}

		if ( hm.getLastScheduled() != null ) {
			SimpleDateFormat formatter =
					new SimpleDateFormat( HospitalityMember.DATE_FORMAT );
			textLastScheduled
					.setText( formatter.format( hm.getLastScheduled() ) );
		}
		else {
			textLastScheduled.setText( "" );
		}

		checkTeamLeader.setSelected( hm.isTeamLeader() );
	}

	/**
	 * Display all members and set last member number.
	 */
	private void displayMembers() {
		int lastMemberNumber = 1;
		membersTextArea.setText( "" );

		if ( !tree.isEmpty() ) {
			for ( int i : tree.keySet() ) {
				HospitalityMember hm = tree.get( i );
				membersTextArea.setText( membersTextArea.getText()
						+ hm.toString() );
				if ( lastMemberNumber <= hm.getMemberNumber() ) {
					lastMemberNumber = hm.getMemberNumber() + 1;
				}
			}
		}

		HospitalityMember.setLastMemberNumber( lastMemberNumber );
	}

	/**
	 * Clear all fields of text.
	 */
	private void clearAll() {
		textNumber.setText( "" + HospitalityMember.getLastMemberNumber() );
		textFirstName.setText( "" );
		textLastName.setText( "" );
		textScheduleWith.setText( "" );
		textSchedWithName.setText( "" );
		textLastScheduled.setText( "" );
		checkTeamLeader.setSelected( false );
		textNumber.requestFocusInWindow();
	}

	/**
	 * ActionListener for update a member
	 */
	private class UpdateListener implements ActionListener {
		public void actionPerformed( ActionEvent e ) {

			// Pull data from form and update tree
			Integer number = Integer.parseInt( textNumber.getText() );
			String first = textFirstName.getText();
			String last = textLastName.getText();
			String schedWithStr = textScheduleWith.getText();
			HospitalityMember schedWith = null;

			if ( schedWithStr != null && !schedWithStr.isEmpty() ) {
				schedWith = checkSchedWith();
				if ( schedWith == null ) {
					return;
				}
			}

			String lastSchedStr = textLastScheduled.getText();
			Date lastSched = null;

			if ( lastSchedStr != null && !lastSchedStr.isEmpty() ) {
				lastSched = checkLastSched();
				if ( lastSched == null ) {
					return;
				}
			}

			boolean leader = checkTeamLeader.isSelected();
			HospitalityMember hm =
					new HospitalityMember( number, first, last, schedWith,
							leader, lastSched );
			tree.put( number, hm );

			// Write to file and update members view
			updateFile();
		}
	}

	/**
	 * Update to file
	 */
	private void updateFile() {
		HospitalityMembersFile.updateFile( frame, tree );
		displayMembers();
		clearAll();
	}

	/**
	 * Clear all text on "new"
	 */
	private class NewListener implements ActionListener {
		@Override
		public void actionPerformed( ActionEvent e ) {
			clearAll();
		}
	}

	/**
	 * Validate a "scheduled with" member
	 * 
	 * @return validated member
	 */
	private HospitalityMember checkSchedWith() {
		String schedWith = textScheduleWith.getText();
		if ( schedWith == null || schedWith.isEmpty() ) {
			return null;
		}

		int num;
		try {
			num = Integer.parseInt( schedWith );
		}
		catch ( NumberFormatException nfe ) {
			String message =
					"Bad number format\n\"" + schedWith + "\" is not a number";
			JOptionPane.showMessageDialog( frame, message, "Bad Number",
					JOptionPane.ERROR_MESSAGE );
			return null;
		}

		Integer number = new Integer( num );
		if ( !tree.containsKey( number ) ) {
			String message = number.toString() + " has not been entered yet";
			JOptionPane.showMessageDialog( frame, message, "Member Not Found",
					JOptionPane.ERROR_MESSAGE );
			return null;
		}

		HospitalityMember schedWithMember = tree.get( number );
		textSchedWithName.setText( schedWithMember.displayFullName() );

		return schedWithMember;
	}

	/**
	 * Validate the "last scheduled date"
	 * 
	 * @return last scheduled date
	 */
	private Date checkLastSched() {
		String lastSchedStr = textLastScheduled.getText();
		Date lastSchedDate = null;
		if ( lastSchedStr == null || lastSchedStr.isEmpty() )
			return null;

		SimpleDateFormat formatter =
				new SimpleDateFormat( HospitalityMember.DATE_FORMAT );

		try {
			lastSchedDate = formatter.parse( lastSchedStr );
		}
		catch ( ParseException e ) {
			String message =
					"Please enter date in " + HospitalityMember.DATE_FORMAT
							+ " format";
			JOptionPane.showMessageDialog( frame, message, "Bad Date Format",
					JOptionPane.ERROR_MESSAGE );
			return null;
		}

		return lastSchedDate;
	}

	/**
	 * Go to the next member record.
	 */
	private void nextRecord() {
		if ( tree == null ) {
			return;
		}

		Integer number = Integer.parseInt( textNumber.getText() );
		Integer key;

		if ( tree.containsKey( number ) ) {
			key = tree.higherKey( number );
		} else {
			key = tree.lastKey();
		}

		if ( key != null ) {
			displayMember( tree.get( key ) );
		}
	}

	/**
	 * Go to the previous member record.
	 */
	private void previousRecord() {
		if ( tree == null ) {
			return;
		}

		Integer number = new Integer( Integer.parseInt( textNumber.getText() ) );
		Integer key;

		if ( tree.containsKey( number ) ) {
			key = tree.lowerKey( number );
		} else {
			key = tree.firstKey();
		}

		if ( key != null ) {
			displayMember( tree.get( key ) );
		}
	}

	/**
	 * Go to a specific member number.
	 */
	private class GotoMenuItemListener implements ActionListener {
		@Override
		public void actionPerformed( ActionEvent e ) {
			String message = "Enter the member number to go to";
			String reply =
					JOptionPane.showInputDialog( frame, message,
							"Go To Member", JOptionPane.OK_CANCEL_OPTION );

			if ( reply.isEmpty() ) {
				return;
			}

			Integer number;
			try {
				number = Integer.parseInt( reply );
			} catch ( NumberFormatException e1 ) {
				message = "Enter a number in the correct format";
				JOptionPane.showMessageDialog( frame, message,
						"Bad Number Format", JOptionPane.ERROR_MESSAGE );
				return;
			}

			if ( !tree.containsKey( number ) ) {
				message = "Member number does not exist";
				JOptionPane.showMessageDialog( frame, message,
						"Does Not Exists", JOptionPane.ERROR_MESSAGE );
				return;
			}

			displayMember( tree.get( number ) );
			tabPane.setSelectedIndex( 0 );
		}
	}

	/**
	 * Execute a search for a member.
	 */
	private class SearchListener implements ActionListener {
		@Override
		public void actionPerformed( ActionEvent e ) {
			Collection<HospitalityMember> members = tree.values();
			findTextArea.setText( "" );
			String findLast = textFindLastName.getText();
			String findFirst = textFindFirstName.getText();

			if ( findLast.isEmpty() && findFirst.isEmpty() ) {
				String message =
						"Please enter last and/or first name and press Search";
				JOptionPane.showMessageDialog( frame, message,
						"Nothing to Search", JOptionPane.INFORMATION_MESSAGE );
			}

			for ( HospitalityMember i : members ) {
				if ( !findLast.isEmpty() ) {
					if ( findLast.equals( i.getLastName() ) ) {
						if ( findFirst.isEmpty() ) {
							findTextArea.append( i.toFind() );
						}
						else {
							if ( findFirst.equals( i.getFirstName() ) )
								findTextArea.append( i.toFind() );
						}
					}
				}
				else {
					if ( findFirst.equals( i.getFirstName() ) ) {
						findTextArea.append( i.toFind() );
					}
				}
			}
		}
	}

}
