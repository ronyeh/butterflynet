package butterflynet.content;

import java.io.File;

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

	public NotesDatabase(File notesPath, File settingsPath) {
		
		DebugUtils.println(notesPath + " " + settingsPath);
	}

}
