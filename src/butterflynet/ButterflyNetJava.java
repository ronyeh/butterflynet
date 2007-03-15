package butterflynet;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import edu.stanford.hci.r3.util.DebugUtils;

/**
 * <p>
 * <span class="BSDLicense"> This software is distributed under the <a
 * href="http://hci.stanford.edu/research/copyright.txt">BSD License</a>. </span>
 * </p>
 * 
 * @author <a href="http://graphics.stanford.edu/~ronyeh">Ron B Yeh</a> (ronyeh(AT)cs.stanford.edu)
 */
public class ButterflyNetJava {

	public static void main(String[] args) {
		DebugUtils.println("Test");
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
