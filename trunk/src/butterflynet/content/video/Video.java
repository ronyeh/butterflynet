package butterflynet.content.video;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
public class Video {

	public static void main(String[] args) {
		File file = new File("MVI_1777.avi");
		String ffmpegCommand = "externalBin/ffmpeg.exe -i \"" + file.getAbsolutePath()
				+ "\" -an -vframes 1 -ss 00:00:04 -y thumbnail%d.jpg";
		run(ffmpegCommand);
	}

	/**
	 * R3 v 4Corners http://moss.stanford.edu/results/103112459
	 */
	private static void run(String command) {
		DebugUtils.println(command);
		// if (true)
		// return;

		try {
			Process proc = Runtime.getRuntime().exec(command);
			InputStream inputStream = proc.getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

			InputStream errorStream = proc.getErrorStream();
			BufferedReader err = new BufferedReader(new InputStreamReader(errorStream));

			String line;
			DebugUtils.println("Errors: ");
			while ((line = err.readLine()) != null) {
				// DebugUtils.println(line);
			}

			DebugUtils.println("----------------------------");

			DebugUtils.println("Output: ");
			while ((line = in.readLine()) != null) {
				// DebugUtils.println(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
