package butterflynet.whiteboard;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import edu.stanford.hci.r3.flash.whiteboard.FlashWhiteboard;
import edu.stanford.hci.r3.pen.Pen;

/**
 * <p>
 * Shows how to use the Flash components to assemble a Live Whiteboard that will get data from the local pen
 * device in real time. We can do multiple Whiteboards at the same time!
 * </p>
 * <p>
 * <span class="BSDLicense"> This software is distributed under the <a
 * href="http://hci.stanford.edu/research/copyright.txt">BSD License</a>. </span>
 * </p>
 * 
 * @author <a href="http://graphics.stanford.edu/~ronyeh">Ron B Yeh</a> (ronyeh(AT)cs.stanford.edu)
 */
public class LiveWhiteboard {
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
		new LiveWhiteboard();
	}

	private FlashWhiteboard flashWhiteboard1;

	public LiveWhiteboard() {
		// in R3, there are two ways to make real-time pen and paper applications... One is to use
		// the Application framework. A second one, which we will use here, is to just attach a pen
		// listener to a live (local) Pen.

		// Connect to two pens, one local, and one accessible through the network
		// Pen pen1 = new Pen(); // local pen

		Pen pen1 = new Pen("Pen 1", "solaria.stanford.edu"); // remote pen
		pen1.setPenServerPort(11105); // forwarded through the nat to machine 1

		// load the Flash component that listens for real-time ink!
		// basically, just open the HTML page that contains the flash component! =)
		// we pick different ports, so the data can be streamed separately
		flashWhiteboard1 = new FlashWhiteboard(8989);
		flashWhiteboard1.addPen(pen1);
		flashWhiteboard1.setSwatchColor(new Color(100, 100, 220));
		flashWhiteboard1.setTitle("Live Whiteboard");
		flashWhiteboard1.load();
	}
}
