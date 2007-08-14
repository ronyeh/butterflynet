package butterflynet.content;

import java.io.File;
import java.util.Date;
import java.util.List;

import papertoolkit.util.DebugUtils;
import papertoolkit.util.files.FileUtils;
import papertoolkit.util.files.SortDirection;


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
public class DocumentsDatabase {

	/**
	 * @param docsPath
	 * @param settingsPath
	 */
	public DocumentsDatabase(File docsPath, File settingsPath) {
		// find all files in docsPath
		List<File> files = FileUtils.listVisibleFilesRecursively(docsPath);
		DebugUtils.println(files);
		
		// find the most recent file in docsPath
		FileUtils.sortByLastModified(files, SortDirection.NEW_TO_OLD);

		for (File f : files) {
			DebugUtils.println(new Date(f.lastModified()) + " " + f);
		}
		DebugUtils.println(files);
		// check the settings to see what was the last file we had processed...
	}

}
