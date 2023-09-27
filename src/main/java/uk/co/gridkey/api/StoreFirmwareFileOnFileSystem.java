package uk.co.gridkey.api;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

public class StoreFirmwareFileOnFileSystem {

	public Object onCall(Map<String, String> uriAttributes, byte[] payload) throws Exception {

		Map<Object, Object> msg = new HashMap<Object, Object>();
		
		Boolean fileStored = false;

		// Extract the required properties
		String fileType = uriAttributes.get("type").toString();
		String revision = uriAttributes.get("revision").toString();
		byte[] rawMessageBytes = payload;

		// Build the file path
		String ftpPath = "/data/ftp";
		String filename = null;

		// Based upon the filename
		switch (fileType) {
		case "hex": {
			filename = "mcumk3.hex";
			break;
		}
		case "crc": {
			filename = "mcumk3.crc";
			break;
		}
		case "boot": {
			filename = "bootup.bin";
			break;
		}
		default: {
			// Do nothing
			break;
		}
		}

		// Considered only to be stored if the file type (filename) is correct
		if (filename != null) {
			try {
				Path filePath = Paths.get(ftpPath, "318", revision, filename);

				// Also only store if the directory does NOT already exist - will not overwrite
				// existing firmware
				if (!Files.exists(filePath)) {
					// Create the directory and sub-directories if they do not exist
					Files.createDirectories(filePath.getParent());

					// Create the stream
					FileOutputStream output = new FileOutputStream(filePath.toFile());

					// Write the raw bytes to the stream
					IOUtils.write(rawMessageBytes, output);

					// Close the output stream
					output.close();

					fileStored = true;
				}
			} catch (Exception e) {
				// Do nothing, error will be reported back on the response i.e. error in
				// completing request
			}
		}

		msg.put("fileStored", fileStored);

		// Return the message back to the flow
		return msg;
	}

}
