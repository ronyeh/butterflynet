package butterflynet.content;

import java.io.File;
import java.util.List;

import butterflynet.ButterflyNet;
import edu.stanford.hci.r3.pen.batch.PenSynchManager;
import edu.stanford.hci.r3.util.DebugUtils;

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

	/**
	 * Access to the parent BNet instance.
	 */
	private ButterflyNet bnet;
	private boolean autoUpdateTimestamps;

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
		// DebugUtils.println(notesPath + " " + settingsPath);

		autoUpdateTimestamps = autoUpdateSynchedFileTimestamp;

		PenSynchManager penSynchManager = new PenSynchManager();
		List<File> newlySynchedFiles = penSynchManager
				.getFilesNewerThan(mostRecentlySynchedTimestamp);
		processNewFiles(newlySynchedFiles);
	}

	private void processNewFiles(List<File> newlySynchedFiles) {

		for (File f : newlySynchedFiles) {
			DebugUtils.println("Processing New File: " + f);

			// once we are done with this file, update the settings with the newest timestamp
			// (advance the marker)
			if (autoUpdateTimestamps) {
				bnet.setMostRecentlySynchedTimestamp(f.lastModified());
				bnet.saveUserSettings();
			} else {
				DebugUtils
						.println("Automatically Update Synched Timestamps is FALSE. This should normally be TRUE. This mode is only for testing.");
			}
		}
	}

}
