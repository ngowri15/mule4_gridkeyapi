package uk.co.gridkey.api;

import java.util.Map;



import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import uk.co.gridkey.datacentre.AbstractJSONFormatter;
import uk.co.gridkey.db.CassandraConnector;
import uk.co.gridkey.db.CassandraSchemaGridKeyMCU;

public class DashboardDataAllMcusJSONFormatter extends AbstractJSONFormatter {
	// Data fields to be extracted and added to the dashboard view from the latest
	// decoded measurement data
	final private String[] dashboardDecodedDataFields = new String[] { "busbar-l1-voltage-mean",
			"busbar-l2-voltage-mean", "busbar-l3-voltage-mean", "busbar-l1-current-mean", "busbar-l2-current-mean",
			"busbar-l3-current-mean", "busbar-neutral-current-mean" };

	/**
	 * Generates the JSON formatted string of MCU dashboard data stored in the
	 * database.
	 * 
	 * @param retrievedData
	 *            The data retrieved from the database
	 * @param dno
	 *            DNO identity
	 * @return String containing the JSON formatted list of MCU dashboard data
	 */
	private String GenerateJSON(Map<String, Map<String, Double>> retrievedData, String dno) {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();

		// The fist elements in the JSON root are fixed format to help when determining
		// what the data represents
		((ObjectNode) rootNode).put("dno", dno);
		((ObjectNode) rootNode).putArray("mcus");

		ArrayNode arrayNode = (ArrayNode) rootNode.get("mcus");

		// Loop through mcus data within the value map
		for (Map.Entry<String, Map<String, Double>> mcuDashboardData : retrievedData.entrySet()) {
			// Declare a tempNode for the element
			JsonNode tempNode = arrayNode.addObject();

			// Get the mcu serial number for the set of dashboard data being processed
			((ObjectNode) tempNode).put("unit-sn", mcuDashboardData.getKey());

			for (String item : dashboardDecodedDataFields) {
				// Extract the specific parameters from the decoded data map to be added to the
				// response
				if (mcuDashboardData.getValue().containsKey(item)) {
					((ObjectNode) tempNode).put(item, mcuDashboardData.getValue().get(item));
				}
			}
		}

		// Pass back a string representation of the JSON
		return rootNode.toString();
	}

	public Object onCall(Map<String, String> uriAttributes, Map<Object, Object> cassConnection) throws Exception {
		
		// Get the fields, which will be used to generate the JSON data
		String dno = uriAttributes.get("dno");
		
		// Get the cassandra connector and get the details required
		CassandraConnector cassConn =  (CassandraConnector) cassConnection.get("cassConn");
		CassandraSchemaGridKeyMCU schema = new CassandraSchemaGridKeyMCU(cassConn);
		Map<String, Map<String, Double>> retrievedData = schema.getDecodedDataLatestAllUnits(dno);

		// Format the data as JSON and pass back to the mule flow
		String jsonData = GenerateJSON(retrievedData, dno);
		//msg.setProperty("jsonData", jsonData, PropertyScope.INVOCATION);

		return jsonData;
	}
}
