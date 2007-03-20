package butterflynet;

import java.awt.Desktop;
import java.awt.SplashScreen;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

import butterflynet.content.NotesDatabase;
import butterflynet.navigation.PageNavigationServer;
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
public class ButterflyNet {

	private static final String PROPERTY_AUTOMATICALLY_UPDATE_SYNCHED_FILE_TIMESTAMP = "AutomaticallyUpdateSynchedFileTimestamp";
	private static final String PROPERTY_TIMESTAMP_OF_MOST_RECENTLY_SYNCHED_FILE = "TimestampOfMostRecentlySynchedFile";

	/**
	 * Just create a new instance! All configuration parameters should be stored in config.ini in
	 * the root directory of ButterflyNet2.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 0) {
			for (String arg : args) {
				if (arg.toLowerCase().equals("redirect")) {
					try {
						FileOutputStream foserr = new FileOutputStream("stderr.log");
						System.setErr(new PrintStream(foserr));
						FileOutputStream fosout = new FileOutputStream("stdout.log");
						System.setOut(new PrintStream(fosout));
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
		}
		new ButterflyNet();
	}

	/**
	 * Automatically advance the marker for figuring out which files we have synched.
	 */
	private boolean autoUpdateSynchedFileTimestamp = true;

	private File bNetDataPath;

	private File clustersPath;

	private File docsPath;

	/**
	 * Stores the system configuration.
	 */
	private final File iniFileSystemConfiguration = new File("config.ini");

	/**
	 * Stores settings for the particular user (for now, it's just one user). These settings will be
	 * overwritten on ButterflyNet exit...
	 */
	private File iniFileUserSettings;

	private long mostRecentlySynchedTimestamp;

	private File notesPath;

	private File pagesPath;

	private File photosPath;

	private File settingsPath;

	private File thumbs100Path;

	private File thumbs128Path;
	private File thumbs256Path;
	private File thumbsPath;
	private PageNavigationServer pageNavigationServer;
	private NotesDatabase notesDatabase;

	/**
	 * The splash screen is shown at startup, before the JVM is invoked! See the program
	 * arguments...
	 */
	public ButterflyNet() {

		// read the properties files, and construct any directories that need to exist
		readConfigProperties();
		readUserSettings();

		// seems to interact with the showFlashGUI method... They are more serial than I'd like...
		SplashScreenUtils.animateSplashScreen(2000, new Point2D.Double(227.5, 140));

		// start checking for data...
		// check for notes, photos, and documents
		// new DocumentsDatabase(docsPath, settingsPath);
		// new PhotosAndVideosDatabase(this);
		notesDatabase = new NotesDatabase(this, notesPath, settingsPath,
				mostRecentlySynchedTimestamp, autoUpdateSynchedFileTimestamp);

		// load the local page navigation server
		pageNavigationServer = new PageNavigationServer(notesDatabase);

		// finally, load the GUI and show the notes, photos, etc...
		showFlashGUI();
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

		pagesPath = new File(notesPath, "Pages");
		clustersPath = new File(notesPath, "Clusters");

		// BNet2Data/Photos
		photosPath = new File(dataPath, "Photos & Videos");

		// BNet2Data/Documents
		docsPath = new File(dataPath, "Documents");

		// BNet2Data/SoftwareSettings
		// For storing thumbnails, file modification dates, search indexes, etc.
		// Anything and everything to help ButterflyNet run better.
		settingsPath = new File(dataPath, "SoftwareSettings");
		// DebugUtils.println("Settings path is hidden? " + settingsPath.isHidden());

		thumbsPath = new File(settingsPath, "Thumbnails");
		thumbs100Path = new File(thumbsPath, "100");
		thumbs128Path = new File(thumbsPath, "128");
		thumbs256Path = new File(thumbsPath, "256");

		// make a list of directories to check
		final File[] makeTheseDirs = new File[] { notesPath, pagesPath, clustersPath, photosPath,
				docsPath, settingsPath, thumbsPath, thumbs100Path, thumbs128Path, thumbs256Path };
		for (File dir : makeTheseDirs) {
			if (!dir.exists()) {
				DebugUtils.println(dir.getName() + " path does not exist. Making the directory.");
				dir.mkdirs();
			}
		}
	}

	public File getClustersPath() {
		return clustersPath;
	}

	public File getPagesPath() {
		return pagesPath;
	}

	public File getPhotosPath() {
		return photosPath;
	}

	public File getThumbnails100Path() {
		return thumbs100Path;
	}

	public File getThumbnails128Path() {
		return thumbs128Path;
	}

	public File getThumbnails256Path() {
		return thumbs256Path;
	}

	public File getThumbnailsPath() {
		return thumbsPath;
	}

	/**
	 * Figure out where the BNetData path is.
	 */
	private void readConfigProperties() {
		final Properties configProperties = new Properties();

		if (iniFileSystemConfiguration.exists()) {
			try {
				// read it from disk
				configProperties.load(new FileInputStream(iniFileSystemConfiguration));
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

		// get user settings...
		iniFileUserSettings = new File(settingsPath, "settings.ini");
	}

	private void readUserSettings() {
		final Properties settingsProperties = new Properties();
		if (iniFileUserSettings.exists()) {
			try {
				// read it from disk
				settingsProperties.load(new FileInputStream(iniFileUserSettings));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// the timestamp of the most recently synched file
			final String recentlySynchedTimestamp = settingsProperties.getProperty(
					PROPERTY_TIMESTAMP_OF_MOST_RECENTLY_SYNCHED_FILE).trim();
			// DebugUtils.println(recentlySynchedTimestamp);
			if (!recentlySynchedTimestamp.equals("")) {
				mostRecentlySynchedTimestamp = Long.parseLong(recentlySynchedTimestamp);
			}

			final String autoUpdateTimestamp = settingsProperties.getProperty(
					PROPERTY_AUTOMATICALLY_UPDATE_SYNCHED_FILE_TIMESTAMP).trim();
			if (!autoUpdateTimestamp.equals("")) {
				autoUpdateSynchedFileTimestamp = Boolean.parseBoolean(autoUpdateTimestamp);
			}
		}
	}

	public void saveUserSettings() {
		final Properties settingsProperties = new Properties();
		settingsProperties.setProperty(PROPERTY_TIMESTAMP_OF_MOST_RECENTLY_SYNCHED_FILE,
				mostRecentlySynchedTimestamp + "");
		settingsProperties.setProperty(PROPERTY_AUTOMATICALLY_UPDATE_SYNCHED_FILE_TIMESTAMP,
				autoUpdateSynchedFileTimestamp + "");
		try {
			settingsProperties.store(new FileOutputStream(iniFileUserSettings), "User Settings");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setMostRecentlySynchedTimestamp(long timestamp) {
		mostRecentlySynchedTimestamp = timestamp;
	}

	/**
	 * Opens the HTML page containing the Flex/Flash GUI.
	 */
	private void showFlashGUI() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (Desktop.isDesktopSupported()) {
						Desktop desktop = Desktop.getDesktop();
						desktop.browse(new File("bin/ButterflyNet2.html").toURI());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
}
