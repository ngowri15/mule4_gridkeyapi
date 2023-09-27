package uk.co.gridkey.api;

import java.io.File;
import java.util.Map;

import java.util.Base64;
import org.apache.commons.io.FileUtils;

import uk.co.gridkey.db.CassandraConnector;
import uk.co.gridkey.db.CassandraSchemaGridKeyMCU;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class HistogramJSONFormatter {

	public Object onCall(Map<String, String> uriAttributes, Map<Object, Object> cassConnection) throws Exception {
		// TODO Auto-generated method stub

		// From the payload, get the raw message bytes; from here, we can get the
		// information required to load the correct JSON file

		// Detect if Windows or Linux as the files will be stored separately from the
		// mule deployment
		// and in different locations depending upon the operating system.
		String jsonFileAndPath = null, jsonString = "";
		
		String dno = uriAttributes.get("dno");
		String mcu = uriAttributes.get("mcu");		
		
		if (System.getProperty("os.name").startsWith("Windows")) {
			// Assume Windows
			jsonFileAndPath = "C:/Lucy/backup/DTF/base_folder/results";
		} else {
			// Assume Linux
			jsonFileAndPath = "/data/DTF/scripts_DTF/results";
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
					base64 = encodeFileToBase64Binary(
							new File(jsonFileAndPath + "/histograms_corrected/" + substationname + "_hist" + ".png"));
					jsonString = GenerateJSON(mcu, substationname, base64);
				} else {
					//msg.setProperty("response", "{\"message\": \"success\"}", PropertyScope.INVOCATION);
					//msg.setProperty("responseCode", 200, PropertyScope.INVOCATION);
					jsonString = GenerateJSON(mcu, substationname, base64);
				}

			} else {
				//msg.setProperty("response", "{\"message\": \"success\"}", PropertyScope.INVOCATION);
				//msg.setProperty("responseCode", 200, PropertyScope.INVOCATION);
				jsonString = GenerateJSON(mcu, "", base64);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return jsonString;
	}

	/**
	 * Generates the JSON string representation of the DTF histogram graphs
	 * 
	 * @param file
	 *            location of the file in the drive
	 * @return base64 String representation of image
	 */

	private String encodeFileToBase64Binary(File file) throws Exception {
		byte[] fileContent = FileUtils.readFileToByteArray(file);
		String encodedString = Base64.getEncoder().encodeToString(fileContent);
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
	 *            Base64 string of histogram image
	 * @return String JSON formatted representation of the base64 data for the
	 *         requested histogram message
	 */
	private String GenerateJSON(String mcu, String substation, String base64) {
		// Create a new ObjectMapper and set the rootNode to a new objectnode
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();

		// The fist elements in the JSON root are fixed format to help when determining
		// what the data represents
		((ObjectNode) rootNode).put("mcu", mcu);
		((ObjectNode) rootNode).put("substation-name", substation);
		((ObjectNode) rootNode).put("base64", base64);

		// Convert the rootNode to a string before returning it
		return rootNode.toString();
	}

}
