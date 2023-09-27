package uk.co.gridkey.api;

import java.util.Map;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import uk.co.gridkey.datacentre.AbstractJSONFormatter;
import uk.co.gridkey.db.CassandraConnector;
import uk.co.gridkey.db.CassandraSchemaGridKeyMCU;

public class McuDetailsJSONFormatter extends AbstractJSONFormatter {
	/**
	 * Generates the JSON formatted string representing the version information data
	 * retrieved from the Cassandra database.
	 * 
	 * @param retrievedData
	 *            The data retrieved from the database
	 * @param dno
	 *            DNO identity
	 * @param mcu
	 *            MCU serial number
	 * @return String containing the JSON representation of the mcu version number
	 *         retrieved from the Cassandra database
	 * @throws JsonProcessingException
	 */
	private String GenerateJSON(Map<String, String> retrievedData) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();

		// Loop through each pair in the Row Key
		for (Map.Entry<String, String> mapEntry : retrievedData.entrySet()) {
			// Put each column into the tempNode
			((ObjectNode) rootNode).put(new String(mapEntry.getKey()), new String(mapEntry.getValue()));
		}

		// Pass back a string representation of the JSON
		return convertNode(rootNode);
	}

	public Object onCall(Map<String, String> uriAttributes, Map<Object, Object> cassConnection) throws Exception {
		
		String jsonData = "{}";
		
		// Get the fields, which will be used to generate the JSON data
		String dno = uriAttributes.get("dno");
		String serialNumber = uriAttributes.get("mcu");
		
		// Get the cassandra connector and get the details required
		CassandraConnector cassConn =  (CassandraConnector) cassConnection.get("cassConn");
		CassandraSchemaGridKeyMCU schema = new CassandraSchemaGridKeyMCU(cassConn);

		// Format the data as JSON and pass back to the mule flow
		jsonData = GenerateJSON(schema.getMcuDetailsSpecificMcu(dno, serialNumber));

		return jsonData;
	}

	private static final ObjectMapper SORTED_MAPPER = new ObjectMapper();
	static {
		SORTED_MAPPER.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
	}

	private String convertNode(final JsonNode node) throws JsonProcessingException {
		final Object obj = SORTED_MAPPER.treeToValue(node, Object.class);
		final String json = SORTED_MAPPER.writeValueAsString(obj);
		return json;
	}
}
