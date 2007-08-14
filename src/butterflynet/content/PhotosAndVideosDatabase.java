package butterflynet.content;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.media.jai.PlanarImage;

import papertoolkit.util.DebugUtils;
import papertoolkit.util.SystemUtils;
import papertoolkit.util.files.FileUtils;
import papertoolkit.util.files.SortDirection;
import papertoolkit.util.graphics.ImageCache;
import papertoolkit.util.graphics.JAIUtils;

import butterflynet.ButterflyNet;

/**
 * <p>
 * Manages the Photos and Videos on disk.
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

	private ButterflyNet bnet;
	private File db;
	private File photosPath;
	private HashMap<File, Long> timestamps;

	public PhotosAndVideosDatabase(ButterflyNet butterflyNet, File thePhotosPath) {
		bnet = butterflyNet;
		photosPath = thePhotosPath;

		DebugUtils.println("Processing Photos and Videos");

		// find all files in docsPath
		List<File> files = FileUtils.listVisibleFilesRecursively(bnet.getPhotosPath());
		// DebugUtils.println(files);

		// find the most recent file in docsPath
		timestamps = FileUtils.sortPhotosByCaptureDate(files, SortDirection.OLD_TO_NEW);

		if (files.size() == 0) {
			return;
		} else {
			// check the settings to see what was the last file we had processed...
			File newestPhoto = files.get(files.size() - 1);
			DebugUtils.println("Newest photo is: " + newestPhoto);

			File oldestPhoto = files.get(0);
			DebugUtils.println("Oldes photo is: " + oldestPhoto);

			// DebugUtils.println("Creating Thumbnails for Photos");
			// createThumbnails(files);

			DebugUtils.println("Saving Timestamps");
			db = new File(bnet.getDatabasePath(), "PhotosAndVideos.txt");
			FileUtils.writeStringToFile(makePhotosXML(files), db);
		}
	}

	/**
	 * @param files
	 * @return
	 */
	public String makePhotosXML(List<File> files) {
		StringBuilder sb = new StringBuilder();
		try {
			sb.append("<photosAndVideos count=\""
					+ files.size() + "\" rootPath=\"" + photosPath.getCanonicalPath() + "\">\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (File f : files) {
			sb.append("<photo path=\"" + FileUtils.getRelativePath(photosPath, f) + "\" time=\""
					+ timestamps.get(f) + "\"/>\n");
		}
		sb.append("</photosAndVideos>");
		String xmlString = sb.toString();
		return xmlString;
	}

	/**
	 * Seems like a reasonable list of thumbnail sizes (pixels on the longer side) is... 100 (like flickr),
	 * 128 pixels (close to facebook's), 256 wide (close to flickr's small).
	 * 
	 * @param files
	 */
	private void createThumbnails(List<File> files) {

		// a thumbnail should be stored in Thumbnails/100, 128, 256
		File thumbnails100Path = bnet.getThumbnails100Path();
		File thumbnails128Path = bnet.getThumbnails128Path();
		File thumbnails256Path = bnet.getThumbnails256Path();

		// int[] sizes = new int[] { 256, 128, 100 };
		// File[] paths = new File[] { thumbnails256Path, thumbnails128Path, thumbnails100Path };

		int[] sizes = new int[] { 128 };
		File[] paths = new File[] { thumbnails128Path };

		for (File f : files) {
			// create a "unique" thumbnail name
			// if two files have the same name, and same file size, and same mod date, might as well
			// assume they are the same! I mean, humans would probably say the same!!!
			String targetThumbnailName = f.getName() + "_" + f.lastModified() + "_" + f.length() + ".jpg";

			if (ContentType.PHOTO.isFileTypeCompatible(f)) {
				// if it's a photo, make a thumbnail using ImageUtils
				// DebugUtils.println(targetThumbnailName);

				PlanarImage image = ImageCache.loadPlanarImage(f);
				for (int i = 0; i < sizes.length; i++) {
					SystemUtils.tic();
					DebugUtils.println(sizes[i]);
					image = JAIUtils.scaleImageToFit(image, sizes[i], sizes[i]);
					JAIUtils.writeImageToJPEG(image, new File(paths[i], targetThumbnailName));
					SystemUtils.toc();
				}
			} else if (ContentType.VIDEO.isFileTypeCompatible(f)) {
				// if it's a video, use ffmpeg to make a thumbnail...
				// DebugUtils.println(targetThumbnailName);

				// xxx do this next...
			}

		}
	}

	public List<File> getListOfPhotosMatching(long firstTimestamp, long lastTimestamp) {
		List<File> matching = new ArrayList<File>();
		for (File f : timestamps.keySet()) {
			long ts = timestamps.get(f);
			if (ts > firstTimestamp && ts < lastTimestamp) {
				matching.add(f);
			}
		}
		return matching;
	}
}
