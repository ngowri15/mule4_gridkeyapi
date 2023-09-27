package uk.co.gridkey.api;

import java.util.HashMap;
import java.util.Map;

import uk.co.gridkey.db.CassandraConnector;
import uk.co.gridkey.db.CassandraSchemaMCU520;

public class TransactionRetrieveUnitDetails{

	public Object onCall(Map<String, String> uriAttributes, Map<Object, Object> cassConnection) throws Exception {

		Map<Object, Object> msg = new HashMap<Object, Object>();

		// Get the flow message

		// Define variables
		Boolean result = false;
		Boolean ipPortPresent = false;
		Boolean unlockCodePresent = false;
		String unitIP = "";
		String unitPort = "";
		String unlockCode = "";

		// Extract the required properties
		String dno = uriAttributes.get("dno");
		String mcu = uriAttributes.get("mcu");

		// Get the Cassandra connector and create a new schema
		CassandraConnector cassConn =  (CassandraConnector) cassConnection.get("cassConn"); 
		CassandraSchemaMCU520 schema = new CassandraSchemaMCU520(cassConn);

		// Get a map containing the serial numbers against IP/Port
		Map<String, String> unitAddresses = schema.getUnitListenerAddressUdp(dno);

		// Check the Unit IP Address
		if (unitAddresses.containsKey(mcu)) {
			// Split the IP and Port
			String[] unitIPAndPort = unitAddresses.get(mcu).split(":");

			// Ensure there are 2 parts; IP and Port
			if (unitIPAndPort.length == 2) {
				// Extract the values
				unitIP = unitIPAndPort[0];
				unitPort = unitIPAndPort[1];

				// Set the properties
				msg.put("unitIP", unitIP);
				msg.put("unitPort", unitPort);

				// Set the IP/Port Result
				ipPortPresent = true;
			}
		}

		// Get the unlock code for the unit
		unlockCode = schema.getUnitUnlockCode(dno, mcu);

		// Check if an entry exists for the MCU and that there is a valid unlock code
		if (!unlockCode.equals("") && (unlockCode.length() == 16)) {
			msg.put("unlockCode", unlockCode);

			// Set the Unlock Code Result
			unlockCodePresent = true;
		}

		// Set the overall result
		if (unlockCodePresent && ipPortPresent) {
			result = true;
		}

		// Pass the result to the flow
		msg.put("ipPortPresent", ipPortPresent);
		msg.put("unlockCodePresent", unlockCodePresent);
		msg.put("result", result);
		msg.put("transactionString", msg.toString());

		// Return the message back to the flow
		return msg;
	}
}
