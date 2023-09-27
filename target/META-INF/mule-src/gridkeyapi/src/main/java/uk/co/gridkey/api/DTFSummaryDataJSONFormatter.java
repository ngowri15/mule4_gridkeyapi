package uk.co.gridkey.api;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import uk.co.gridkey.db.CassandraConnector;
import uk.co.gridkey.db.CassandraSchemaGridKeyMCU;

import com.fasterxml.jackson.databind.node.ArrayNode;

public class DTFSummaryDataJSONFormatter {

	public Object onCall(Map<String, String> uriAttributes, Map<Object, Object> cassConnection) throws Exception {
		// TODO Auto-generated method stub

		// From the payload, get the raw message bytes; from here, we can get the
		// information required to load the correct JSON file
		String xlsxPath = "", jsonString = "";
		
		String dno = uriAttributes.get("dno");
		
		// Get the cassandra connector and get the details required
		CassandraConnector cassConn =  (CassandraConnector) cassConnection.get("cassConn");
		CassandraSchemaGridKeyMCU schema = new CassandraSchemaGridKeyMCU(cassConn);
		
		if (System.getProperty("os.name").startsWith("Windows")) {
			// Assume Windows
			xlsxPath = "C:/Lucy/backup/DTF/base_folder/results_" + dno + "/summary_results_corrected.xlsx";
		} else {
			// Assume Linux
			xlsxPath = "/data/DTF/scripts_DTF/results_" + dno + "/summary_results_corrected.xlsx";
		}

		
		List<Map<String, String>> summaryData = null;
		Map<String, String> filteredData = null;
		List<Map<String, String>> mcuDetails = schema.getMcuDetails(dno);
		if (mcuDetails.size() > 0) {
			CustomUtils objUtils = new CustomUtils();
			summaryData = objUtils.readExcelFile(xlsxPath);
			if (summaryData.size() > 0) {
				filteredData = objUtils.filterMCUDetailWithSubstationName(mcuDetails);
				if (filteredData.size() > 0) {
					jsonString = GenerateJSON(dno, summaryData, filteredData);
				} else {
					jsonString = GenerateJSON(dno, summaryData, filteredData);
				}
			} else {
				jsonString = GenerateJSON(dno, summaryData, filteredData);
			}
		} else {
			jsonString = GenerateJSON(dno, summaryData, filteredData);
		}

		return jsonString;
	}

	/**
	 * Generates the JSON string representation of the DTF histogram graphs
	 * 
	 * @param dno
	 * @param summaryData
	 *            required data from xlsx
	 * @return String JSON formatted representation of the DTF Summary Data
	 */
	private String GenerateJSON(String dno, List<Map<String, String>> summaryData, Map<String, String> filteredData)
			throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();

		// The fist elements in the JSON root are fixed format to help when determining
		// what the data represents
		((ObjectNode) rootNode).put("dno", dno);
		((ObjectNode) rootNode).putArray("summary");

		ArrayNode arrayNode = (ArrayNode) rootNode.get("summary");
		String mcuNumber = "";
		// For each item in the received data map, extract the row key and insert the
		// data into the "data" array nodes

		if (summaryData != null && filteredData != null) {
			for (Map<String, String> mcusummary : summaryData) {
				// Declare a tempNode for the element
				JsonNode tempNode = arrayNode.addObject();

				// Loop through each pair in the Row Key
				for (Map.Entry<String, String> mapEntry : mcusummary.entrySet()) {
					// Put each column into the tempNode
					((ObjectNode) tempNode).put(new String(mapEntry.getKey()), new String(mapEntry.getValue()));
					if (mapEntry.getKey().equals("substation-name")) {
						mcuNumber = filteredData.get(mapEntry.getValue()) != null
								? filteredData.get(mapEntry.getValue())
								: "";
						((ObjectNode) tempNode).put("unit_sn", mcuNumber);
					}
				}
			}
		}

		String status = "";
		if (summaryData == null || summaryData.size() == 0)
			status = "Unable to read the summary excel or no data available";

		if (filteredData == null || filteredData.size() == 0)
			status = "Unable to parse the Substation data from MCU details";

		((ObjectNode) rootNode).put("status", (status.equals("")) ? "success" : status);

		// Pass back a string representation of the JSON
		return convertNode(rootNode);
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
