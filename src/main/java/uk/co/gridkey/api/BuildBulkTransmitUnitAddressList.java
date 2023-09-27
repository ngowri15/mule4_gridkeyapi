package uk.co.gridkey.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.co.gridkey.db.CassandraConnector;
import uk.co.gridkey.db.CassandraSchemaMCU520;

public class BuildBulkTransmitUnitAddressList {

	@SuppressWarnings("unchecked")
	public Object onCall(Map<String, String> uriAttributes, String payload, Map<Object, Object> cassConnection) throws Exception {

		Map<Object, Object> msg = new HashMap<Object, Object>();
		
		String dno = uriAttributes.get("dno");
		List<String> mcuList = null;
		List<String> transmitAddressList = new ArrayList<String>();

		try {
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> decodedJson = mapper.readValue(payload, Map.class);
			mcuList = (List<String>) decodedJson.get("mcus");

		} catch (Exception e) {
			// Do nothing, mcuList will be null, and therefore can be handled
		}

		if (mcuList != null) {
			// Get the Cassandra connector and create a new schema
			CassandraConnector cassConn =  (CassandraConnector) cassConnection.get("cassConn");
			CassandraSchemaMCU520 schema = new CassandraSchemaMCU520(cassConn);

			// Get a map containing the serial numbers against IP/Port
			Map<String, String> allUnitAddresses = schema.getUnitListenerAddressUdp(dno);

			for (Map.Entry<String, String> entry : allUnitAddresses.entrySet()) {
				String mcu = entry.getKey();

				if (mcuList.contains(mcu)) {
					// In the list, so sanity check it contains a colon (as it is IP and port
					// separated by a colon)
					if (entry.getValue().contains(":")) {
						transmitAddressList.add(entry.getValue());
					}
				}
			}
		}

		// Pass the result to the flow
		msg.put("transmitAddressList", transmitAddressList);

		// Return the message back to the flow
		return msg;
	}
}
