package uk.co.gridkey.api;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ConstructPrepreparedTransactionRequest {
	
	public Object onCall(String payload) throws Exception {

		Map<Object, Object> msg = new HashMap<Object, Object>();
		
		// Create a new Jackson mapper object and use it to read the json string into a
		// Map.
		ObjectMapper mapper = new ObjectMapper();
		@SuppressWarnings("unchecked")
		Map<String, Object> decodedJson = mapper.readValue(payload, Map.class);
		String transactionString = null;

		try {
			transactionString = (String) decodedJson.get("transactionString");

			if (!transactionString.contains("GingerbreadLatte")) {
				transactionString = "GingerbreadLatte " + transactionString;
			}
		} catch (Exception e) {
			// Do nothing, transaction string being null is sufficient for now
		}

		// Pass the result to the flow
		msg.put("transactionString", transactionString);
		return msg;
	}
}
