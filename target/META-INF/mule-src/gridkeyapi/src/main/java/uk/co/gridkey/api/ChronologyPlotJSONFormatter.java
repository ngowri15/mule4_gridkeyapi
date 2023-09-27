package uk.co.gridkey.api;

import java.io.File;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import uk.co.gridkey.db.CassandraConnector;
import uk.co.gridkey.db.CassandraSchemaGridKeyMCU;

public class ChronologyPlotJSONFormatter {

	public Object onCall(Map<String, String> uriAttributes, Map<Object, Object> cassConnection) throws Exception {
		// TODO Auto-generated method stub

		// From the payload, get the raw message bytes; from here, we can get the
		// information required to load the correct JSON file
		// Detect if Windows or Linux as the files will be stored separately from the
		// mule deployment
		// and in different locations depending upon the operating system.
		String jsonFileAndPath = "", jsonString = "";
		String xlsxPath = "";
		
		String dno = uriAttributes.get("dno");
		String mcu = uriAttributes.get("mcu");
				
		if (System.getProperty("os.name").startsWith("Windows")) {
			// Assume Windows
			jsonFileAndPath = "C:/Lucy/backup/DTF/base_folder/results_" + dno;
			xlsxPath = "C:/Lucy/backup/DTF/base_folder/results_" + dno + "/summary_results_corrected.xlsx";
		} else {
			// Assume Linux
			jsonFileAndPath = "/data/DTF/scripts_DTF/results_" + dno;
			xlsxPath = "/data/DTF/scripts_DTF/results_" + dno + "/summary_results_corrected.xlsx";
		}
		String base64 = "No data found";
		try {
			File f = new File(jsonFileAndPath);
			if (f.exists() && f.isDirectory()) {

				CassandraConnector cassConn =  (CassandraConnector) cassConnection.get("cassConn");
				CassandraSchemaGridKeyMCU schema = new CassandraSchemaGridKeyMCU(cassConn);

				// read substation name from cassandra using mcu number

				Map<String, String> mcudesc = schema.getDescriptions(dno);
				String substationname = mcudesc.get(mcu);
				if (substationname != null && !substationname.isEmpty()) {
					//msg.setProperty("valid", "true", PropertyScope.INVOCATION);
					base64 = encodeFileToBase64Binary(new File(
							jsonFileAndPath + "/time_histograms_corrected/" + substationname + "_hist_time" + ".png"));
					CustomUtils objUtils = new CustomUtils();
					List<Map<String, String>> summaryData = objUtils.readExcelFile(xlsxPath);
					Map<String, String> substationMap = objUtils.filterSummaryBySubstation(substationname, summaryData);
					jsonString = GenerateJSON(mcu, substationname, base64, substationMap,
							feederInformation(schema, dno, mcu));
				} else {
					jsonString = GenerateJSON(mcu, substationname, base64, null, null);
				}

			} else {
				jsonString = GenerateJSON(mcu, "", base64, null, null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return jsonString;
	}

	private String encodeFileToBase64Binary(File file) throws Exception {
		String encodedString = "No data found";
		if (file.exists()) {
			byte[] fileContent = FileUtils.readFileToByteArray(file);
			encodedString = Base64.getEncoder().encodeToString(fileContent);
		}
		return encodedString;
	}

	/**
	 * Generates the JSON string representation of the DTF histogram graphs
	 * 
	 * @param mcu
	 *            MCU serial number
	 * @param substation
	 *            Substation name
	 * @param base64
	 *            Base64 string of Chronology Plotted image
	 * @param substationMap
	 *            map with specific substation details
	 * @return String JSON formatted representation of the chronology graphs
	 */
	private String GenerateJSON(String mcu, String substation, String base64, Map<String, String> substationMap,
			Map<String, String> feederInformation) {
		// Create a new ObjectMapper and set the rootNode to a new objectnode
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();

		// The fist elements in the JSON root are fixed format to help when determining
		// what the data represents
		((ObjectNode) rootNode).put("mcu", mcu);

		if (substationMap != null) {
			for (Map.Entry<String, String> mapEntry : substationMap.entrySet()) {
				// Put each column into the tempNode
				((ObjectNode) rootNode).put(new String(mapEntry.getKey()), new String(mapEntry.getValue()));
			}
			for (Map.Entry<String, String> feederMapEntry : feederInformation.entrySet()) {
				// Put each column into the tempNode
				((ObjectNode) rootNode).put(new String(feederMapEntry.getKey()), new String(feederMapEntry.getValue()));
			}
		}

		((ObjectNode) rootNode).put("base64", base64);

		// Convert the rootNode to a string before returning it
		return rootNode.toString();
	}

	private Map<String, String> feederInformation(CassandraSchemaGridKeyMCU schema, String dno, String mcu) {
		Map<String, String> result = new HashMap<String, String>();
		String feeder1Enabled = schema.getFeederEnabled(dno, mcu, 1);
		String feeder2Enabled = schema.getFeederEnabled(dno, mcu, 2);
		String feeder3Enabled = schema.getFeederEnabled(dno, mcu, 3);
		String feeder4Enabled = schema.getFeederEnabled(dno, mcu, 4);
		String feeder5Enabled = schema.getFeederEnabled(dno, mcu, 5);
		String feeder6Enabled = schema.getFeederEnabled(dno, mcu, 6);
		String feeder1Name = schema.getFeederName(dno, mcu, 1);
		String feeder2Name = schema.getFeederName(dno, mcu, 2);
		String feeder3Name = schema.getFeederName(dno, mcu, 3);
		String feeder4Name = schema.getFeederName(dno, mcu, 4);
		String feeder5Name = schema.getFeederName(dno, mcu, 5);
		String feeder6Name = schema.getFeederName(dno, mcu, 6);

		result.put("feeder-1-enabled", feeder1Enabled);
		result.put("feeder-2-enabled", feeder2Enabled);
		result.put("feeder-3-enabled", feeder3Enabled);
		result.put("feeder-4-enabled", feeder4Enabled);
		result.put("feeder-5-enabled", feeder5Enabled);
		result.put("feeder-6-enabled", feeder6Enabled);

		// Add the feeder names
		result.put("feeder-1-name", feeder1Name);
		result.put("feeder-2-name", feeder2Name);
		result.put("feeder-3-name", feeder3Name);
		result.put("feeder-4-name", feeder4Name);
		result.put("feeder-5-name", feeder5Name);
		result.put("feeder-6-name", feeder6Name);

		return result;
	}

}
