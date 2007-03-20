package butterflynet;

import java.awt.Desktop;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

import butterflynet.content.DocumentsDatabase;
import butterflynet.content.NotesDatabase;
import butterflynet.content.PhotosAndVideosDatabase;
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

	private File bNetDataPath;

	/**
	 * Stores the system configuration.
	 */
	private final File CONFIG_INI = new File("config.ini");

	private File docsPath;

	private File notesPath;

	private File photosPath;

	private File settingsPath;

	private File thumbs100Path;

	private File thumbs128Path;

	private File thumbs256Path;

	private File thumbsPath;

	/**
	 * The splash screen is shown at startup, before the JVM is invoked! See the program
	 * arguments...
	 */
	public ButterflyNet() {

		// read the properties
		readConfigProperties();

		SplashScreenUtils.animateSplashScreen(5000, new Point2D.Double(227.5, 140));

		// load the GUI
		// show notes and photos from LAST time... and start populating the data in the background

		// start checking for data...
		// check for notes, photos, and documents
		// new DocumentsDatabase(docsPath, settingsPath);
		// new PhotosAndVideosDatabase(this);
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
		final File[] makeTheseDirs = new File[] { notesPath, photosPath, docsPath, settingsPath,
				thumbsPath, thumbs100Path, thumbs128Path, thumbs256Path };
		for (File dir : makeTheseDirs) {
			if (!dir.exists()) {
				DebugUtils.println(dir.getName() + " path does not exist. Making the directory.");
				dir.mkdirs();
			}
		}
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
