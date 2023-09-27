package uk.co.gridkey.api;

import java.nio.file.Path;
import java.util.Map;

public class ConstructFirmwareUpgradeTransactionRequest {

	
	public Object onCall(Map<Object, Object>  getFirmwareFileDetailsFromFileSystem) throws Exception {
		
		// Define the variables
		String transactionCode = null;
		String transactionString = "";

		// Get the required fields
		Path ftpPath = (Path) getFirmwareFileDetailsFromFileSystem.get("ftpPath");
		String fileSize =  (String) getFirmwareFileDetailsFromFileSystem.get("fileSize");
		String fileExtension = (String) getFirmwareFileDetailsFromFileSystem.get("extension");

		// Get the correct transaction code
		switch (fileExtension) {
		case "hex": {
			transactionCode = "3302";
			break;
		}
		case "crc": {
			transactionCode = "3301";
			break;
		}
		case "bin": {
			transactionCode = "330F";
			break;
		}
		default: {
			// Check to see if the file extension contains hex in some other format, as that
			// is the only
			// other valid type of firmware upgrade file at the moment. But it may be a
			// split hex file
			// due to its size. See this Confluence article:
			// "https://lucyelectricgridkey.atlassian.net/wiki/spaces/GKKB/pages/2718335010/Data+Centre+Remote+Upgrade+AutoLoop+Software"
			if (fileExtension.contains("hex")) {
				// Is it the first part of the file?
				if (fileExtension.endsWith("hex00")) {
					transactionCode = "3302";
				} else {
					// Doesn't end in hex00, probably therefore one of the other parts that ends in
					// hex01,
					// hex02 etc, in which case send a different transaction request so that the
					// device
					// knows to append it
					transactionCode = "3303";
				}
			}

			break;
		}
		}

		// Build the transaction string
		if (transactionCode != null) {
			transactionString = String.format("GingerbreadLatte %s %s %s", transactionCode, fileSize,
					ftpPath.toString().replace("\\", "/"));
		} else {
			// Just send something that will not cause issues like a 5200
			transactionString = "GingerbreadLatte 5200";
		}
		
		return transactionString;
	}

}
