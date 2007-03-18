package butterflynet.content;

import java.io.File;
import java.util.List;

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
public class PhotosDatabase {

	public PhotosDatabase(File photosPath, File settingsPath) {
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
	}

}
