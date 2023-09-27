package uk.co.gridkey.api;

import java.util.Map;

public class ConstructMCU520TransactionRequestWithParams {
	
	public Object onCall(Map<String, String> uriAttributes, Map<String, String> validateTransactionRequest, Map<String, String> transactionRetrieveUnitDetails) throws Exception {
		// Get the flow message

		// Define the variables
		String transactionString = "";

		// Get the required fields
		String params = uriAttributes.get("params");
		String transactionCode = validateTransactionRequest.get("transactionCode");
		String unlockCode = transactionRetrieveUnitDetails.get("unlockCode");


		// Build the transaction string. Assume that previous steps have validated the
		// params field
		transactionString = String.format("%s %s %s", unlockCode, transactionCode, params);

		// Return the transaction string to the flow

		// Return the message back to the flow
		return transactionString;
	}
}
