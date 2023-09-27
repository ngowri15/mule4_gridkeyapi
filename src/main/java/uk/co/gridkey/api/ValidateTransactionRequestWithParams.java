package uk.co.gridkey.api;

import java.util.HashMap;
import java.util.Map;

public class ValidateTransactionRequestWithParams {

	public Object onCall(Map<String, String> uriAttributes, String payload) throws Exception {
		
		Map<Object, Object> msg = new HashMap<Object, Object>();

		// Extract the required properties
		String transactionType = uriAttributes.get("type");
		String params = payload;

		// Define variables
		Boolean result = true;
		String transactionCode = null;

		// Validate the transaction request, but also the associated parameters for it
		switch (transactionType) {
		case "mcu-reset": {
			transactionCode = "31";

			// Check what parameter has been supplied and see if it is semantically correct
			if (params.toLowerCase().equals("quick") || params.toLowerCase().equals("mmcu")) {
				result = false;
			}
		}
			break;

		case "periodic-request": {
			transactionCode = "32";

			// Check what parameter has been supplied. Should be 5 characters long as it
			// represents a 16-bit number
			if (params.length() != 5) {
				result = false;
			}
		}
			break;

		case "phone-number-update": {
			transactionCode = "33";

			// Perform some simple checks, such as whether correct format
			if (params.startsWith("+44")) {
				result = false;
			}
		}
			break;

		case "alert-request": {
			transactionCode = "36";

			// Check what parameter has been supplied. Should be 5 characters long as it
			// represents an 8-bit number
			if (params.length() != 3) {
				result = false;
			}
		}
			break;

		default: {
			result = false;
		}
			break;
		}

		// Pass the result etc to the flow
		msg.put("result", result);
		msg.put("transactionCode", transactionCode);

		// Return the message back to the flow
		return msg;
	}

}
