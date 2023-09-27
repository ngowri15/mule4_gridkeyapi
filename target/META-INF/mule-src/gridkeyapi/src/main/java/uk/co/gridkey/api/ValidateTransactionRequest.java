package uk.co.gridkey.api;

import java.util.HashMap;
import java.util.Map;

public class ValidateTransactionRequest {

	public Object onCall(Map<String, String> uriAttributes) throws Exception {
		
		Map<Object, Object> msg = new HashMap<Object, Object>();
		
		// Extract the required properties
		String transactionType = uriAttributes.get("type");

		// Define variables
		Boolean result = true;
		String transactionCode = null;

		switch (transactionType) {
		case "mcu-reset":
			transactionCode = "31";
			break;

		case "date-time-update":
			transactionCode = "34";
			break;

		case "status-request":
			transactionCode = "35";
			break;

		case "store-context":
			transactionCode = "37";
			break;

		default:
			result = false;
			break;
		}

		// Pass the result etc to the flow
		msg.put("result", result);
		msg.put("transactionCode", transactionCode);

		// Return the message back to the flow
		return msg;
	}

}
