package uk.co.gridkey.api;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import uk.co.gridkey.datacentre.AbstractJSONFormatter;
import uk.co.gridkey.db.CassandraConnector;
import uk.co.gridkey.db.CassandraSchemaMCU520;

public class DataAvailableYearlyJSONFormatter extends AbstractJSONFormatter {
	/**
	 * Generates the JSON formatted string representing the decoded message data
	 * retrieved from the Cassandra database.
	 * 
	 * @param retrievedData
	 *            Decoded payload data from the Cassandra database
	 * @param dno
	 *            DNO identity
	 * @param mcu
	 *            MCU serial number
	 * @param year
	 *            The years' worth of data which was retrieved from the database
	 * @param end
	 *            End timestamp for the purposes of populating the JSON header and
	 *            identifying the data range contained within it
	 * @return String containing the JSON representation of the payload data
	 *         returned from the Cassandra database
	 */
	private String GenerateJSON(Map<Integer, Map<Integer, String>> retrievedData, String dno, String mcu, String year) {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();

		// The fist elements in the JSON root are fixed format to help when determining
		// what the data represents
		((ObjectNode) rootNode).put("dno", dno);
		((ObjectNode) rootNode).put("mcu", mcu);
		((ObjectNode) rootNode).put("year", year);

		// Loop through each parameter in the map
		for (Map.Entry<Integer, Map<Integer, String>> monthData : retrievedData.entrySet()) {
			String monthStr = monthData.getKey().toString();

			// Create a node object for the month
			ArrayNode monthNode = ((ObjectNode) rootNode).putArray(monthStr);

			// Loop through each days worth of data within the value map
			for (Map.Entry<Integer, String> dayData : monthData.getValue().entrySet()) {
				String dayStr = dayData.getKey().toString();

				// Put the data into the node
				monthNode.add(dayStr);
			}
		}

		// Pass back a string representation of the JSON
		return rootNode.toString();
	}

	public Object onCall(Map<String, String> uriAttributes, Map<Object, Object> cassConnection) throws Exception {
		
		String jsonData = "{}";

		// Get the fields, which will be used to generate the JSON data				
		String dno = uriAttributes.get("dno");
		String serialNumber = uriAttributes.get("mcu");
		String year = uriAttributes.get("year");
		
		// Get the cassandra connector and get the details required
		CassandraConnector cassConn =  (CassandraConnector) cassConnection.get("cassConn");
		CassandraSchemaMCU520 schema = new CassandraSchemaMCU520(cassConn);

		// Get the data from the database
		Map<Integer, Map<Integer, String>> result = schema.getDataAvailableForYear(dno, serialNumber,
				Integer.parseInt(year));

		// Format the data as JSON and pass back to the mule flow
		jsonData = GenerateJSON(result, dno, serialNumber, year);

		return jsonData;
	}

}
