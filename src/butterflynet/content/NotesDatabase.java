package butterflynet.content;

import java.io.File;
import java.util.List;

import papertoolkit.pattern.coordinates.PageAddress;
import papertoolkit.pen.ink.Ink;
import papertoolkit.pen.synch.PenSynch;
import papertoolkit.pen.synch.PenSynchManager;
import papertoolkit.util.DebugUtils;
import papertoolkit.util.files.FileUtils;
import butterflynet.ButterflyNet;

/**
 * <p>
 * 
 * </p>
 * <p>
 * <span class="BSDLicense"> This software is distributed under the <a
 * href="http://hci.stanford.edu/research/copyright.txt">BSD License</a>. </span>
 * </p>
 * 
 * @author <a href="http://graphics.stanford.edu/~ronyeh">Ron B Yeh</a> (ronyeh(AT)cs.stanford.edu)
 * 
 */
public class NotesDatabase {

	private boolean autoUpdateTimestamps;
	/**
	 * Access to the parent BNet instance.
	 */
	private ButterflyNet bnet;
	private int currentPageIndex;
	private List<File> pageDirs;
	private File pagesPath;

	/**
	 * @param butterflyNet
	 * @param notesPath
	 * @param settingsPath
	 * @param mostRecentlySynchedTimestamp
	 * @param autoUpdateSynchedFileTimestamp
	 */
	public NotesDatabase(ButterflyNet butterflyNet, File notesPath, File settingsPath,
			long mostRecentlySynchedTimestamp, boolean autoUpdateSynchedFileTimestamp) {
		bnet = butterflyNet;
		pagesPath = bnet.getPagesPath();
		// DebugUtils.println(notesPath + " " + settingsPath);

		autoUpdateTimestamps = autoUpdateSynchedFileTimestamp;

		PenSynchManager penSynchManager = new PenSynchManager();
		List<File> newlySynchedFiles = penSynchManager.getFilesNewerThan(mostRecentlySynchedTimestamp);
		processNewFiles(newlySynchedFiles);

		// now, figure out what pages exist... and maintain a list that we can navigate
		buildDatabaseOfPages();
	}

	/**
	 * 
	 */
	private void buildDatabaseOfPages() {
		pageDirs = FileUtils.listVisibleDirs(pagesPath);
		DebugUtils.println("Found these Pages: " + pageDirs);
		currentPageIndex = 0;
	}

	public File getCurrPageDir() {
		if (pageDirs.size() == 0) {
			return new File("."); // a file that says... there are no notes! =)
		}
		return pageDirs.get(currentPageIndex);
	}

	public File getNextPageDir() {
		if (pageDirs.size() == 0) {
			return new File("."); // a file that says... there are no notes! =)
		}

		currentPageIndex++;
		if (currentPageIndex == pageDirs.size()) {
			currentPageIndex = 0; // wrap
		}
		return pageDirs.get(currentPageIndex);
	}

	public File getPrevPageDir() {
		if (pageDirs.size() == 0) {
			return new File("."); // a file that says... there are no notes! =)
		}

		currentPageIndex--;
		if (currentPageIndex < 0) {
			currentPageIndex = pageDirs.size() - 1; // wrap
		}
		return pageDirs.get(currentPageIndex);
	}

	/**
	 * @param newlySynchedFiles
	 */
	private void processNewFiles(List<File> newlySynchedFiles) {

		for (File f : newlySynchedFiles) {
			DebugUtils.println("Processing New File: " + f);

			PenSynch penSynch = new PenSynch(f);
			List<Ink> importedInk = penSynch.getImportedInk();
			for (Ink ink : importedInk) {

				DebugUtils.println("Processing ink from: " + ink.getSourcePageAddress());
				PageAddress address = ink.getSourcePageAddress();
				String addressStr = address.toString();

				// make the directory to hold this page's synchs. One directory per page (overkill,
				// yes, but makes things easier)
				File pageDirectory = new File(pagesPath, "page_" + addressStr);
				pageDirectory.mkdirs(); // make sure it exists

				// if the destination file exists already, we increment the synchIndex and write a
				// new file... this enables us to distinguish synchs!
				int synchIndex = 0;
				File destFile = new File(pageDirectory, "page_" + addressStr + "_s" + synchIndex + ".xml");
				while (destFile.exists()) {
					synchIndex++;
					destFile = new File(pageDirectory, "page_" + addressStr + "_s" + synchIndex + ".xml");
				}
				DebugUtils.println("Looking for: " + destFile.getAbsolutePath() + ". Does it exist? ["
						+ destFile.exists() + "]");
				// the file had better not exist by this point

				// serialize the ink to that page file!
				ink.saveToXMLFile(destFile);
			}

			// once we are done with this file, update the settings with the newest timestamp
			// (advance the marker)
			if (autoUpdateTimestamps) {
				bnet.setMostRecentlySynchedTimestamp(f.lastModified());
				bnet.saveUserSettings();
			} else {
				DebugUtils.println("Automatically Update Synched Timestamps is FALSE. "
						+ "This should normally be TRUE. This mode is only for testing.");
			}
		}
	}

}
