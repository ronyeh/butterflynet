package butterflynet.content;

import java.io.File;
import java.util.List;

import net.sf.fmj.ui.application.PlayerPanel;
import edu.stanford.hci.r3.util.DebugUtils;
import edu.stanford.hci.r3.util.files.FileUtils;
import edu.stanford.hci.r3.util.files.SortDirection;

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
public class PhotosAndVideosDatabase {

	public PhotosAndVideosDatabase(File photosPath, File settingsPath) {
		// find all files in docsPath
		List<File> files = FileUtils.listVisibleFilesRecursively(photosPath);
		// DebugUtils.println(files);

		// find the most recent file in docsPath
		FileUtils.sortPhotosByCaptureDate(files, SortDirection.NEW_TO_OLD);

		// SystemUtils.tic();
		// for (File f : files) {
		// DebugUtils.println(f);
		// }
		// SystemUtils.toc();

		// check the settings to see what was the last file we had processed...
		File newestPhoto = files.get(0);
		DebugUtils.println("Newest photo is: " + newestPhoto);

		createPhotoThumbnails(files);
	}

	/**
	 * Seems like a reasonable list of thumbnail sizes (pixels on the longer side) is... 100 (like
	 * flickr), 128 pixels (close to facebook's), 256 wide (close to flickr's small).
	 * 
	 * @param files
	 */
	private void createPhotoThumbnails(List<File> files) {

		for (File f : files) {
			if (f.getName().toLowerCase().endsWith(".avi")) {
				DebugUtils.println(f);
			}
		}

		// Failed Attempt at processing video...
		// Use FMJ instead of JMF (or JVLC)...
		// file://C:\Documents and Settings\Ron Yeh\My Documents\Projects\ButterflyNet2\MVI_1769.AVI
		// String movie = "../Data/BNet2Data/Photos & Videos/Ron's/March 22, 2005/MVI_1777.AVI";
		// File movieFile = new File(movie);
		// PlayerPanel.main(new String[] {});
		
		
		// create a "unique" thumbnail name based on the current file name,
	}
}
