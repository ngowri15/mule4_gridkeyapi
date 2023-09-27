package uk.co.gridkey.api;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class CheckConfigExistsOnFileSystem {

	public Object onCall(Map<String, String> uriAttributes) throws Exception {
		
		Map<Object, Object> msg = new HashMap<Object, Object>();
		
		// Get the flow message
		Boolean fileExists = false;

		// Extract the required properties
		String dno = uriAttributes.get("dno");
		String mcu = uriAttributes.get("mcu");
		String configType = uriAttributes.get("type");

		// Build the file path
		// TODO change FTP root directory
		String ftpRoot = "/data/ftp";

		// Build the file path (the path on the system)
		// and the ftp path (the path from the ftp root - to send to the mcu)
		Path filePath = Paths.get(ftpRoot, dno, mcu, "out", configType + ".bin");
		Path ftpPath = Paths.get(dno, mcu, "out", configType + ".bin");

		// Check to see if the file exists and set the flow variables as required
		if (Files.exists(filePath)) {
			msg.put("ftpPath", ftpPath);
			msg.put("fileSize", String.format("%06d", Files.size(filePath)));
			fileExists = true;
		}

		// Return if the file exists as this is used in the flow
		msg.put("fileExists", fileExists);

		// Return the message back to the flow
		return msg;
	}

}
