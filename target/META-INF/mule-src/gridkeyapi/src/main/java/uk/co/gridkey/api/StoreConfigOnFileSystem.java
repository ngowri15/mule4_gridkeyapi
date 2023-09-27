package uk.co.gridkey.api;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

public class StoreConfigOnFileSystem {

	public Object onCall(Map<String, String> uriAttributes, Object payload) throws Exception{

		Map<Object, Object> msg = new HashMap<Object, Object>();

		// Get the flow message
		// Extract the required properties
		
		
		String dno = uriAttributes.get("dno");
		String mcu = uriAttributes.get("mcu");
		String configType = uriAttributes.get("type");
		byte[] rawMessageBytes = (byte[]) payload;

		// Build the file path
		String ftpRoot = "/data/ftp";
		Path filePath = Paths.get(ftpRoot, dno, mcu, "out", configType + ".bin");

		// Create the directory and sub-directories if they do not exist
		Files.createDirectories(filePath.getParent());

		// Create the stream
		FileOutputStream output = new FileOutputStream(filePath.toFile());

		// Write the raw bytes to the stream
		IOUtils.write(rawMessageBytes, output);
		msg.put("output", output);

		// Close the output stream
		output.close();

		// Return the message back to the flow
		return msg;
	}

}
