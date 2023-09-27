package uk.co.gridkey.api;

import java.io.File;
import java.util.Base64;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.io.FileUtils;

public class Downloader {

	public Object onCall(Map<String, String> uriAttributes) throws Exception {

		// From the payload, get the raw message bytes; from here, we can get the
		// information required to return the correct JSON file
		String strDecodeFolderPath = "", filePath = "", basePath = "", contentDisp = "reports", base64String = "",
				jsonString = "";

		String param = uriAttributes.get("param");
		
		if (System.getProperty("os.name").startsWith("Windows")) {
			// Assume Windows
			basePath = "C:/Lucy/Processing/reports/";
		} else {
			// Assume Linux
			basePath = "/data/reports/";
		}
		if (param != null) {
			strDecodeFolderPath = decrypt(param);
			filePath = basePath + strDecodeFolderPath;
			File targetFile = new File(filePath);
			if (targetFile.exists()) {
				String[] tmp = strDecodeFolderPath.split("/");
				if (tmp.length > 0)
					contentDisp = tmp[tmp.length - 1];

				base64String = encodeFileToBase64Binary(targetFile);
				jsonString = GenerateJSON("success", base64String, contentDisp);
			} else
				jsonString = GenerateJSON("Requested File not found", base64String, contentDisp);
		} else
			jsonString = GenerateJSON("Required param missing", base64String, contentDisp);

		return jsonString;
	}

	private String GenerateJSON(String status, String base64, String fileName) {
		// Create a new ObjectMapper and set the rootNode to a new objectnode
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();

		// The fist elements in the JSON root are fixed format to help when determining
		// what the data represents
		((ObjectNode) rootNode).put("status", status);
		((ObjectNode) rootNode).put("base64", base64);
		((ObjectNode) rootNode).put("filename", fileName);

		// Convert the rootNode to a string before returning it
		return rootNode.toString();
	}

	public String decrypt(String originalInput) {
		byte[] decodedBytes = Base64.getDecoder().decode(originalInput);
		return new String(decodedBytes);
	}

	private String encodeFileToBase64Binary(File file) throws Exception {
		byte[] fileContent = FileUtils.readFileToByteArray(file);
		String encodedString = Base64.getEncoder().encodeToString(fileContent);
		return encodedString;
	}
}
