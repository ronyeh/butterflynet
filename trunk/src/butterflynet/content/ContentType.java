package butterflynet.content;

import java.io.File;

/**
 * The data types that exist in our system.
 * 
 * <p>
 * This software is distributed under the <a href="http://hci.stanford.edu/research/copyright.txt">
 * BSD License</a>.
 * </p>
 * 
 * @author <a href="http://graphics.stanford.edu/~ronyeh">Ron B Yeh</a> ( ronyeh(AT)cs.stanford.edu )
 */
public enum ContentType {

	AUDIO("Audio", new String[] { "MP3", "WAV" }),

	DOCUMENT("Document", new String[] { "RTF", "TXT", "DOC", "PDF", "HTML" }),

	// note: only JPEG/JPG files contain EXIF information
	PHOTO("Photo", new String[] { "JPEG", "JPG", "PNG", "GIF", "BMP", "TIFF" }),

	SPREADSHEET("SpreadSheet", new String[] { "XLS", "CSV" }),

	VIDEO("Video", new String[] { "AVI", "MOV", "MPG", "FLV", "MP4" });

	private String[] extensions;

	// case sensitive name
	// a database may use this case insensitively
	// a file system would probably use this case sensitively (MS-DOS/Windows is probably a special case)
	private String name;

	// constructor of the enumerated type
	ContentType(String myName, String[] acceptedExtensions) {
		name = myName;
		extensions = acceptedExtensions;
	}

	/**
	 * @return
	 */
	private String[] getExtensions() {
		return extensions;
	}

	/**
	 * Checks if the file is of this type, by doing a file extension check.
	 * 
	 * @param file
	 * @return
	 */
	public boolean isFileTypeCompatible(final File file) {
		final String fileName = file.getName().toLowerCase();
		for (final String s : getExtensions()) {
			if (fileName.endsWith(s.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return name;
	}

}