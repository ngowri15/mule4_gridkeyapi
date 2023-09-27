package uk.co.gridkey.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Map;

public class PredictionDataJSONFormatter {
	Process mProcess;

	public Object onCall(Map<String, String> uriAttributes) throws Exception {
		// TODO Auto-generated method stub

		// From the payload, get the raw message bytes; from here, we can get the
		// information required to return the correct JSON file
		
		String basePath = "", scriptName = "", command = "python3.9 ", allParams = "";
		
		String dno = uriAttributes.get("dno");
		String mcu = uriAttributes.get("mcu");
		String startDate = uriAttributes.get("start");
		String endDate = uriAttributes.get("end");
		String param = uriAttributes.get("param");		

		if (System.getProperty("os.name").startsWith("Windows")) {
			// Assume Windows
			basePath = "C:/Lucy/Processing/reports/src/";
			command = "python ";
		} else {
			// Assume Linux
			basePath = "/data/reports/scripts/";
			command = "python3.9 ";
		}

		try {
			if (param.equals("zsp"))
				scriptName = param + ".py";

			if (!scriptName.equals("")) {
				allParams = mcu + " " + dno + " " + startDate + " " + endDate;
				command = command + basePath + scriptName + " " + allParams;

				File file = new File(basePath + scriptName);
				if (file.exists()) {
					String response = executeCommand(command);
					ObjectMapper mapper = new ObjectMapper();
					JsonNode actualObj = mapper.readTree(response);
					return convertNode(actualObj);
				}
			}

		} catch (Exception e) {
			return "failed";
		}
		return "failed";
	}

	public String executeCommand(String command) {
		StringBuffer output = new StringBuffer();
		Process p;

		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return output.toString();
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
