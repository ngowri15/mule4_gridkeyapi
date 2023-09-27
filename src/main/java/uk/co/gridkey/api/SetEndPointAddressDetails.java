package uk.co.gridkey.api;

import java.util.HashMap;
import java.util.Map;

public class SetEndPointAddressDetails {

	
	public Object onCall(String payload) throws Exception {

		Map<Object, Object> msg = new HashMap<Object, Object>();
		
		String unitIp = null;
		String unitPort = null;

		try {
			String combineIpPortString = payload;

			// Try and split the string
			String[] temp = combineIpPortString.split(":");

			if (temp.length == 2) {
				unitIp = temp[0];
				unitPort = temp[1];
			}
		} catch (Exception e) {
			// Do nothing, having a null string for unit IP and port will be sufficient
		}

		// Pass the result back to the flow
		msg.put("unitIp", unitIp);
		msg.put("unitPort", unitPort);

		// Return the message back to the flow
		return msg;
	}

}
