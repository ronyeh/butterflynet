package butterflynet;

import java.awt.Desktop;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import butterflynet.content.DocumentsDatabase;
import butterflynet.content.NotesDatabase;
import butterflynet.content.PhotosDatabase;
import edu.stanford.hci.r3.util.DebugUtils;
import edu.stanford.hci.r3.util.graphics.SplashScreenUtils;

/**
 * <p>
 * <span class="BSDLicense"> This software is distributed under the <a
 * href="http://hci.stanford.edu/research/copyright.txt">BSD License</a>. </span>
 * </p>
 * 
 * @author <a href="http://graphics.stanford.edu/~ronyeh">Ron B Yeh</a> (ronyeh(AT)cs.stanford.edu)
 */
public class ButterflyNetJava {

	/**
	 * Just create a new instance! All configuration parameters should be stored in config.ini in
	 * the root directory of ButterflyNet2.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		new ButterflyNetJava();
	}

	private File bNetDataPath;

	/**
	 * Stores the system configuration.
	 */
	private final File CONFIG_INI = new File("config.ini");

	private File settingsPath;

	private File docsPath;

	private File photosPath;

	private File notesPath;

	/**
	 * The splash screen is shown at startup, before the JVM is invoked! See the program
	 * arguments...
	 */
	public ButterflyNetJava() {

		// read the properties
		readConfigProperties();

		SplashScreenUtils.animateSplashScreen(5000, new Point2D.Double(227.5, 140));

		// load the GUI
		// show notes and photos from LAST time... and start populating the data in the background

		// start checking for data...
		// check for notes, photos, and documents
		new DocumentsDatabase(docsPath, settingsPath);
		new PhotosDatabase(photosPath, settingsPath);
		new NotesDatabase(notesPath, settingsPath);
	}

	/**
	 * Make sure all the required files/directories exist.
	 * 
	 * @param dataPath
	 */
	private void checkValidityOfDirectoryStructure(File dataPath) {
		if (!dataPath.exists()) {
			DebugUtils.println("Data Path does not exist: " + dataPath + ". Making the directory.");
			dataPath.mkdirs();
		}

		// BNet2Data/Notes
		notesPath = new File(dataPath, "Notes");
		if (!notesPath.exists()) {
			DebugUtils.println("Notes Path does not exist: " + notesPath
					+ ". Making the directory.");
			notesPath.mkdir();
		}

		// BNet2Data/Photos
		photosPath = new File(dataPath, "Photos & Videos");
		if (!photosPath.exists()) {
			DebugUtils.println("Photos & Videos path does not exist: " + photosPath
					+ ". Making the directory.");
			photosPath.mkdir();
		}

		// BNet2Data/Documents
		docsPath = new File(dataPath, "Documents");
		if (!docsPath.exists()) {
			DebugUtils.println("Documents Path does not exist: " + docsPath
					+ ". Making the directory.");
			docsPath.mkdir();
		}

		// BNet2Data/SoftwareSettings
		// For storing thumbnails, file modification dates, search indexes, etc.
		// Anything and everything to help ButterflyNet run better.
		settingsPath = new File(dataPath, "SoftwareSettings");
		if (!settingsPath.exists()) {
			DebugUtils.println("Settings Path does not exist: " + settingsPath
					+ ". Making the directory.");
			settingsPath.mkdir();
		}
		// DebugUtils.println("Settings path is hidden? " + settingsPath.isHidden());
	}

	/**
	 * Figure out where the BNetData path is.
	 */
	private void readConfigProperties() {
		final Properties configProperties = new Properties();

		if (CONFIG_INI.exists()) {
			try {
				// read it from disk
				configProperties.load(new FileInputStream(CONFIG_INI));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// the BNetData directory
			final String bNetDataDir = configProperties.getProperty("BNetData").trim();
			if (!bNetDataDir.equals("")) {
				bNetDataPath = new File(bNetDataDir);
			}
			DebugUtils.println("Found BNetData path: " + bNetDataPath);
		}

		if (bNetDataPath == null) {
			// assume a default BNetData directory.
			bNetDataPath = new File("../Data/BNet2Data");
		}

		// check the validity of this directory
		// it must exist, and contain a few sub directories...
		checkValidityOfDirectoryStructure(bNetDataPath);
	}

	/**
	 * Opens the HTML page containing the Flex/Flash GUI.
	 */
	private void showFlashGUI() {
		try {
			if (Desktop.isDesktopSupported()) {
				Desktop desktop = Desktop.getDesktop();
				desktop.browse(new File("bin/ButterflyNet2.html").toURI());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
