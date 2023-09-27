package uk.co.gridkey.api;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class GetFirmwareFileDetailsFromFileSystem {

	
	public Object onCall(Map<String, String> uriAttributes) throws Exception {

		Map<Object, Object> msg = new HashMap<Object, Object>();
		
		Boolean fileExists = false;

		// Extract the required properties
		String fileExtension = uriAttributes.get("extension");
		String revision = uriAttributes.get("revision");

		// Build the file path
		String ftpRoot;
		if (System.getProperty("os.name").startsWith("Windows")) {
			// Assume Windows
			ftpRoot = "D:/data/ftp";
		} else {
			// Assume Linux
			ftpRoot = "/data/ftp";
		}

		File[] directoryFiles = new File(ftpRoot + "/318/" + revision).listFiles();

		// Find the file based upon the extension provided
		String filename = findFileFromExtension(directoryFiles, fileExtension);

		if (filename != null) {
			// Build the file path (the path on the system)
			// and the ftp path (the path from the ftp root - to send to the mcu)
			Path filePath = Paths.get(ftpRoot, "318", revision, filename);
			Path ftpPath = Paths.get("318", revision, filename);

			// Check to see if the file exists and set the flow variables as required
			if (Files.exists(filePath)) {
				msg.put("ftpPath", ftpPath);
				msg.put("fileSize", String.format("%06d", Files.size(filePath)));
				fileExists = true;
			}
		}

		// Return if the file exists as this is used in the flow
		msg.put("fileExists", fileExists);

		// Return the message back to the flow
		return msg;
	}

	private String findFileFromExtension(File[] directoryFiles, String fileType) {
		String filename = null;

		if ((directoryFiles != null) && (fileType != null)) {
			for (File file : directoryFiles) {
				if (file.isFile() && file.getName().endsWith(fileType)) {
					filename = file.getName();
				}
			}
		}

		return filename;
	}

}
