package net.snortum.hospitality;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * A utility class for various static methods.
 * 
 * @author Knute Snortum, (c) copyright 2011-2013
 * @version 1.0
 * 
 */
class Utility {
	
	// All members are static
	private Utility() {
	}

	/**
	 * Look and feel, helper for {@link Schedule#start()} and
	 * {@link HospitalityMembers#start()}.
	 */
	public static void installLookAndFeel() {
		UIManager.LookAndFeelInfo[] lafInfo =
				UIManager.getInstalledLookAndFeels();

		for ( int i = 0; i < lafInfo.length; i++ ) {
			if ( lafInfo[i].getName().equals( "Windows" ) ) {
				try {
					UIManager.setLookAndFeel( lafInfo[i].getClassName() );
				}
				catch ( ClassNotFoundException e ) {
					e.printStackTrace(); // TODO Logger for all these exceptions
				}
				catch ( InstantiationException e ) {
					e.printStackTrace();
				}
				catch ( IllegalAccessException e ) {
					e.printStackTrace();
				}
				catch ( UnsupportedLookAndFeelException e ) {
					e.printStackTrace();
				}
			}
		}
	}

}
