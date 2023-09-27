package uk.co.gridkey.api;

import java.nio.file.Path;
import java.util.Map;

public class ConstructConfigTransactionRequest {

	public Object onCall(Map<Object, Object> checkConfigExistsOnFileSystem, Map<Object, Object> transactionRetrieveUnitDetails, Map<String, String> uriAttributes) throws Exception {
		
		// Get the flow message

		// Define the variables
		String transactionCode = "";
		String transactionString = "";

		// Get the required fields
		Path ftpPath = (Path) checkConfigExistsOnFileSystem.get("ftpPath");
		String fileSize = (String) checkConfigExistsOnFileSystem.get("fileSize");
		String unlockCode = (String) transactionRetrieveUnitDetails.get("unlockCode");
		String configType = (String) transactionRetrieveUnitDetails.get("type");

		// Get the correct transaction code
		switch (configType) {

		case "fact":
			transactionCode = "1A";
			break;

		case "user":
			transactionCode = "1B";
			break;
		}

		// Build the transaction string
		transactionString = String.format("%s %s %s %s", unlockCode, transactionCode, fileSize,
				ftpPath.toString().replace("\\", "/"));

		// Return the transaction string to the flow
		//msg.setPayload(transactionString);

		// Return the message back to the flow
		return transactionString;
	}

}
