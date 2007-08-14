package butterflynet.navigation;

import java.io.File;
import java.util.List;

import papertoolkit.flash.FlashCommand;
import papertoolkit.flash.FlashCommunicationServer;
import papertoolkit.pen.ink.Ink;
import papertoolkit.util.DebugUtils;
import papertoolkit.util.files.FileUtils;

import butterflynet.content.NotesDatabase;
import butterflynet.content.PhotosAndVideosDatabase;

/**
 * <p>
 * This server will relay Ink objects to the Flash UI, which will listen for them.
 * 
 * This is a skeleton implementation. Later on, we will allow our event handlers and content filters to live
 * in the world of Flash, for faster UI prototyping.
 * </p>
 * <p>
 * <span class="BSDLicense"> This software is distributed under the <a
 * href="http://hci.stanford.edu/research/copyright.txt">BSD License</a>. </span>
 * </p>
 * 
 * @author <a href="http://graphics.stanford.edu/~ronyeh">Ron B Yeh</a> (ronyeh(AT)cs.stanford.edu)
 */
public class FlashServer {

	/**
	 * communicate through this port
	 */
	public static final int DEFAULT_PORT = 7545;

	private FlashCommunicationServer flash;

	private NotesDatabase notesDB;

	private PhotosAndVideosDatabase photosDB;

	/**
	 * @param notesDatabase
	 * @param photosDatabase
	 */
	public FlashServer(NotesDatabase notesDatabase, PhotosAndVideosDatabase photosDatabase) {
		notesDB = notesDatabase;
		photosDB = photosDatabase;
		flash = new FlashCommunicationServer(DEFAULT_PORT);

		flash.addCommand("next", new FlashCommand() {
			@Override
			public void invoke(String... args) {
				handleNext();
			}
		});
		flash.addCommand("prev", new FlashCommand() {
			@Override
			public void invoke(String... args) {
				handlePrev();
			}
		});
		flash.addCommand("connected", new FlashCommand() {
			@Override
			public void invoke(String... args) {
				handleCurrent();
			}
		});
	}

	/**
	 * @param prevPageDir
	 */
	private void composeAndSendInk(File prevPageDir) {
		// figure out which files are there... and send them in XML back to the Flash GUI...
		List<File> pageFiles = FileUtils.listVisibleFiles(prevPageDir);
		DebugUtils.println("Sending these page files back: " + pageFiles);
		Ink allInk = new Ink();
		flash.sendMessage(makeInkXMLMessageOfPageFiles(pageFiles, allInk));
		
		List<File> listOfPhotosMatching = photosDB.getListOfPhotosMatching(allInk.getFirstTimestamp(), allInk.getLastTimestamp());
		flash.sendMessage(makePhotosXMLMessageOfPhotoFiles(listOfPhotosMatching));
	}

	private String makePhotosXMLMessageOfPhotoFiles(List<File> listOfPhotosMatching) {
		return photosDB.makePhotosXML(listOfPhotosMatching);
	}

	private void handleCurrent() {
		File nextPageDir = notesDB.getCurrPageDir();
		composeAndSendInk(nextPageDir);
	}

	private void handleNext() {
		File nextPageDir = notesDB.getNextPageDir();
		composeAndSendInk(nextPageDir);
	}

	private void handlePrev() {
		File prevPageDir = notesDB.getPrevPageDir();
		composeAndSendInk(prevPageDir);
	}

	/**
	 * Turn the files into ink that I will send to the flash client! Crazyyy....
	 * 
	 * @param pageFiles
	 * @param allInk
	 * @return
	 */
	private String makeInkXMLMessageOfPageFiles(List<File> pageFiles, Ink allInk) {
		long minTS = Long.MAX_VALUE;
		long maxTS = Long.MIN_VALUE;
		for (File f : pageFiles) {
			Ink pageInk = new Ink(f);
			minTS = Math.min(pageInk.getFirstTimestamp(), minTS);
			maxTS = Math.max(pageInk.getLastTimestamp(), maxTS);
			allInk.append(pageInk);
		}
		return allInk.toXMLString(false);
	}

	/**
	 * Sends a list of files over to flash... What will we use this for?
	 * 
	 * @param pageFiles
	 * @return
	 */
	private String makeXMLMessageOfPageFiles(List<File> pageFiles) {
		StringBuilder xml = new StringBuilder();
		xml.append("<pageFiles>");
		{
			for (File f : pageFiles) {
				xml.append("<file path=\"" + f.getAbsolutePath() + "\"/>");
			}
		}
		xml.append("</pageFiles>");
		return xml.toString();
	}

	public void showFlashGUI(File apolloGuiFile) {
		flash.openFlashApolloGUI(apolloGuiFile);
	}

}
