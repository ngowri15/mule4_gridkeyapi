package uk.co.gridkey.api;

import java.util.Map;

public class ConstructMCU520TransactionRequest {

	public Object onCall(Map<String, String> validateTransactionRequest, Map<String, String> transactionRetrieveUnitDetails) throws Exception {

		// Get the flow message

		// Define the variables
		String transactionString = "";

		// Get the required fields
		String transactionCode = validateTransactionRequest.get("transactionCode");
		String unlockCode = transactionRetrieveUnitDetails.get("unlockCode");

		// Build the transaction string
		transactionString = String.format("%s %s", unlockCode, transactionCode);

		// Return the transaction string to the flow

		// Return the message back to the flow
		return transactionString;
	}

}
