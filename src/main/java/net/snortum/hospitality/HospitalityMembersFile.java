package net.snortum.hospitality;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jnlp.FileContents;
import javax.jnlp.FileOpenService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Deals with finding, opening, loading, and updating the data file.
 * 
 * @author Knute Snortum, (c) copyright 2011-2013
 * @version 1.1
 * 
 */
public class HospitalityMembersFile {

	// Constants
	private final static String DEFAULT_FILE_NAME = "HospitalityMembers.txt";
	private final static boolean RUNNING_IN_JNLP = false;
	//@formatter:off
	private final static String RECORD_FORMAT =
			"^(\\d+)\t"                  // number
			+ "([^\t]+)\t"               // first name
			+ "([^\t]+)\t"               // last name
			+ "(\\d+)?\t"                // schedule with
			+ "(true|false)\t"           // team leader
			+ "(\\d{2}/\\d{2}/\\d{4})?"; // last scheduled
	//@formatter:on
	private final static SimpleDateFormat FORMATTER =
			new SimpleDateFormat( HospitalityMember.DATE_FORMAT );

	// Fields
	private static File file;
	private static FileReader fileReader;
	private static String fileName;

	private static FileOpenService fos;
	private static FileContents fc;

	// Getters and setters

	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @param myFile
	 *            the file to set
	 */
	public void setFile( File myFile ) {
		file = myFile;
	}

	/**
	 * @return the fileReader
	 */
	public FileReader getFileReader() {
		return fileReader;
	}

	/**
	 * @param myFileReader
	 *            the fileReader to set
	 */
	public void setFileReader( FileReader myFileReader ) {
		fileReader = myFileReader;
	}

	/**
	 * @return the fileName
	 */
	public static String getFileName() {
		return fileName;
	}

	/**
	 * @param myFileName
	 *            the fileName to set
	 */
	public void setFileName( String myFileName ) {
		fileName = myFileName;
	}

	/**
	 * Open file on local file system or while running JNLP
	 * 
	 * @param frame
	 *            - the container to display the error in
	 */
	public static void openFile( JFrame frame ) {
		if ( RUNNING_IN_JNLP ) {
			openFileJnlp( frame );
		}
		else {
			openFileLocalFS( frame );
		}
	}

	/**
	 * Open file contents in a JNLP environment
	 */
	private static void openFileJnlp( JFrame frame ) {
		try {
			fos = (FileOpenService) ServiceManager
					.lookup( "javax.jnlp.FileOpenService" );
		}
		catch ( UnavailableServiceException e ) {
			String message = "FileOpenService not available: " + e.getMessage()
					+ "\nProgram will abort";
			JOptionPane.showMessageDialog( frame, message,
					"Cannot Create File", JOptionPane.ERROR_MESSAGE );
			System.exit( 1 );
		}

		try {
			// ask user to select a file through this service
			fc = fos.openFileDialog( null, new String[] { "txt" } );
		}
		catch ( IOException e1 ) {
			String message =
					"Cannot get file content: " + e1.getMessage()
							+ "\nProgram will abort";
			JOptionPane.showMessageDialog( frame, message,
					"Cannot Create File", JOptionPane.ERROR_MESSAGE );
			System.exit( 1 );
		}
	}

	/**
	 * Open data file, helper method for {@link Schedule#start()} and
	 * {@link HospitalityMembers#start()}
	 * 
	 * @param frame
	 *            - JFrame in which to display the errors
	 */
	private static void openFileLocalFS( JFrame frame ) {
		fileName = DEFAULT_FILE_NAME;
		file = new File( fileName );

		if ( !file.exists() ) {
			try {
				file.createNewFile();
			}
			catch ( IOException e ) {
				String message =
						"Cannot create file " + fileName
								+ "\nProgram will abort";
				JOptionPane.showMessageDialog( frame, message,
						"Cannot Create File", JOptionPane.ERROR_MESSAGE );
				System.exit( 1 );
			}
		}

		try {
			fileReader = new FileReader( fileName );
		}
		catch ( FileNotFoundException e1 ) {
			String message =
					"Cannot open file " + fileName
							+ " for reading\nProgram will abort";
			JOptionPane.showMessageDialog( frame, message,
					"Cannot Create File", JOptionPane.ERROR_MESSAGE );
			System.exit( 1 );
		}
	}

	/**
	 * Load records from file, helper method for {@link Schedule#start()} and
	 * {@link HospitalityMembers#start()}
	 * 
	 * @param frame
	 *            - JFrame for errors
	 * @return tree - treeMap of memberId => member object
	 */
	public static TreeMap<Integer, HospitalityMember>
			loadRecords( JFrame frame ) {

		TreeMap<Integer, HospitalityMember> tree =
				new TreeMap<Integer, HospitalityMember>();
		Map<Integer, Integer> schedWithHash = new HashMap<Integer, Integer>();
		Pattern p = Pattern.compile( RECORD_FORMAT, Pattern.CASE_INSENSITIVE );

		BufferedReader br;
		InputStream is;

		if ( RUNNING_IN_JNLP ) {
			is = null;
			try {
				is = fc.getInputStream();
			}
			catch ( IOException e ) {
				String message =
						"Getting InputStream from FileContent: "
								+ e.getMessage();
				JOptionPane.showMessageDialog( frame, message,
						"InputStream Error", JOptionPane.ERROR_MESSAGE );
				return null;
			}
			br = new BufferedReader( new InputStreamReader( is ) );
		}
		else {
			br = new BufferedReader( fileReader );
		}

		// Read lines from the text file
		while ( true ) {
			String line;

			try {
				line = br.readLine();
			}
			catch ( IOException e1 ) {
				String message = "IO Exception reading " + file.getName();
				JOptionPane.showMessageDialog( frame, message, "Bad Read",
						JOptionPane.ERROR_MESSAGE );
				return null;
			}

			if ( line == null ) {
				break;
			}

			// parse the line into a hospitality object
			Matcher m = p.matcher( line );

			if ( !m.find() ) {
				String message =
						"Bad record format\n\"" + line + "\"\nRecord skipped";
				JOptionPane.showMessageDialog( frame, message, "Bad Record",
						JOptionPane.WARNING_MESSAGE );
				continue;
			}

			int memberNo = Integer.parseInt( m.group( 1 ) );
			String first = m.group( 2 );
			String last = m.group( 3 );

			if ( m.group( 4 ) != null ) {
				// can't do "scheduleWith" yet, save values
				schedWithHash.put( memberNo, Integer.parseInt( m.group( 4 ) ) );
			}

			boolean leader = Boolean.parseBoolean( m.group( 5 ) );
			String dateStr = m.group( 6 );
			Date lastSched = null;

			if ( dateStr != null ) {
				try {
					lastSched = FORMATTER.parse( dateStr );
				}
				catch ( ParseException e4 ) {
					lastSched = null;
				}
			}

			tree.put( memberNo, new HospitalityMember( memberNo, first, last,
					null, leader, lastSched ) );

		} // end Read Lines

		try {
			br.close();

			if ( RUNNING_IN_JNLP ) {
				is.close();
			}
			else {
				fileReader.close();
			}
		}
		catch ( IOException e5 ) {
		}

		// put "scheduleWith" into tree now
		if ( !schedWithHash.isEmpty() ) {
			for ( int i : schedWithHash.keySet() ) {
				HospitalityMember hm = tree.get( i );
				Integer schedNum = schedWithHash.get( i );
				HospitalityMember schedWith = tree.get( schedNum );
				hm.setScheduleWith( schedWith ); // OK if it's null
				tree.put( i, hm );
			}
		}

		return tree;
	}

	/**
	 * Update the file from the tree, helper method for {@link Schedule#start()}
	 * and {@link HospitalityMembers#start()}
	 * 
	 * @param frame
	 *            - JFrame to display error in
	 * @param tree
	 *            - treeMap of memberId => member object
	 */
	public static void updateFile( JFrame frame,
			Map<Integer, HospitalityMember> tree ) {

		if ( tree == null ) {
			String message = "Nothing to write to file";
			JOptionPane.showMessageDialog( frame, message, "Nothing to write",
					JOptionPane.WARNING_MESSAGE );
			return;
		}

		BufferedWriter bw;
		OutputStream os;

		if ( RUNNING_IN_JNLP ) {
			try {
				os = fc.getOutputStream( true );
			}
			catch ( IOException e ) {
				String message =
						"Getting OutputStream from FileContent: "
								+ e.getMessage();
				JOptionPane.showMessageDialog( frame, message,
						"OutputStream Error", JOptionPane.ERROR_MESSAGE );
				return;
			}

			bw = new BufferedWriter( new OutputStreamWriter( os ) );
		}
		else {
			try {
				bw = new BufferedWriter( new FileWriter( fileName ) );
			}
			catch ( IOException e1 ) {
				String message =
						"Cannot open file \"" + fileName + "\" for writing";
				JOptionPane.showMessageDialog( frame, message,
						"File Open Error", JOptionPane.ERROR_MESSAGE );
				return;
			}
		}

		for ( HospitalityMember myHM : tree.values() ) {
			try {
				bw.write( myHM.toFile() );
			}
			catch ( IOException e2 ) {
				String message = "Writing to file: " + e2.getMessage();
				JOptionPane.showMessageDialog( frame, message,
						"File Write Error", JOptionPane.ERROR_MESSAGE );
			}
		}

		if ( RUNNING_IN_JNLP ) {
			try {
				os.close();
			}
			catch ( IOException e3 ) {
			}
		}

		try {
			bw.close();
		}
		catch ( IOException e4 ) {
		}
	}

}
