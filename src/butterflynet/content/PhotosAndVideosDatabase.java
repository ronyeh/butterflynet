package butterflynet.content;

import java.io.File;
import java.util.List;

import javax.media.jai.PlanarImage;

import butterflynet.ButterflyNet;
import edu.stanford.hci.r3.util.DebugUtils;
import edu.stanford.hci.r3.util.SystemUtils;
import edu.stanford.hci.r3.util.files.FileUtils;
import edu.stanford.hci.r3.util.files.SortDirection;
import edu.stanford.hci.r3.util.graphics.ImageCache;
import edu.stanford.hci.r3.util.graphics.JAIUtils;

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

	private ButterflyNet butterflyNet;

	public PhotosAndVideosDatabase(ButterflyNet bNet) {
		butterflyNet = bNet;

		// find all files in docsPath
		List<File> files = FileUtils.listVisibleFilesRecursively(butterflyNet.getPhotosPath());
		// DebugUtils.println(files);

		// find the most recent file in docsPath
		FileUtils.sortPhotosByCaptureDate(files, SortDirection.NEW_TO_OLD);

		if (files.size() > 0) {
			// check the settings to see what was the last file we had processed...
			File newestPhoto = files.get(0);
			DebugUtils.println("Newest photo is: " + newestPhoto);

			createThumbnails(files);
		}
	}

	/**
	 * Seems like a reasonable list of thumbnail sizes (pixels on the longer side) is... 100 (like flickr),
	 * 128 pixels (close to facebook's), 256 wide (close to flickr's small).
	 * 
	 * @param files
	 */
	private void createThumbnails(List<File> files) {

		// a thumbnail should be stored in Thumbnails/100, 128, 256
		File thumbnails100Path = butterflyNet.getThumbnails100Path();
		File thumbnails128Path = butterflyNet.getThumbnails128Path();
		File thumbnails256Path = butterflyNet.getThumbnails256Path();

		int[] sizes = new int[] { 256, 128, 100 };
		File[] paths = new File[] { thumbnails256Path, thumbnails128Path, thumbnails100Path };

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
}
