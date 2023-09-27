package uk.co.gridkey.api;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import uk.co.gridkey.bean.ReportsDataBean;

import com.fasterxml.jackson.databind.node.ArrayNode;

public class ReportsDataJSONFormatter {

	public Object onCall(Map<String, String> uriAttributes) throws Exception {
		// TODO Auto-generated method stub

		// From the payload, get the raw message bytes; from here, we can get the
		// information required to return the correct JSON file
		
		String basePath = "", jsonString = "";
		
		String dno = uriAttributes.get("dno");
		String mcu = uriAttributes.get("mcu");		

		if (System.getProperty("os.name").startsWith("Windows")) {
			// Assume Windows
			basePath = "C:/Lucy/Processing/reports" + "/" + dno + "/" + mcu;
		} else {
			// Assume Linux
			basePath = "/data/reports" + "/" + dno + "/" + mcu;
		}

		// check the base path exists or not
		String key = "", url = "", domainURL = "https://api.gridkey.cloud/v2/downloadreports/";
		Map<String, ArrayList<ReportsDataBean>> listAllData = new HashMap<String, ArrayList<ReportsDataBean>>();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		File directory = new File(basePath);
		FileFilter directoryFileFilter = new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory();
			}
		};
		if (directory.exists() && directory.isDirectory()) {

			// check for list of folders in the reports directory
			File[] directoryListAsFile = directory.listFiles(directoryFileFilter);
			if (directoryListAsFile.length > 0) {
				for (File folder : directoryListAsFile) {
					key = folder.getName();
					url = dno + "/" + mcu + "/" + key;
					directory = new File(folder.getAbsolutePath());
					if (directory.exists() && directory.isDirectory()) {
						ArrayList<ReportsDataBean> listData = new ArrayList<ReportsDataBean>();
						File[] list = directory.listFiles();
						for (File allfiles : list) {
							ReportsDataBean bean = new ReportsDataBean();
							bean.setFileName(allfiles.getName().replace(".pdf", ""));
							bean.setFullPath(domainURL + encrypt(url + "/" + allfiles.getName()));
							bean.setTimeStamp(format.format(allfiles.lastModified()));
							listData.add(bean);
						}
						listAllData.put(key, listData);
					}
				}
			}

			if (listAllData.size() > 0)
				jsonString = GenerateJSON(dno, mcu, listAllData);
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
	private String GenerateJSON(String dno, String mcu, Map<String, ArrayList<ReportsDataBean>> datalist)
			throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();

		// The fist elements in the JSON root are fixed format to help when determining
		// what the data represents
		((ObjectNode) rootNode).put("dno", dno);
		((ObjectNode) rootNode).put("mcu", mcu);

		for (Map.Entry<String, ArrayList<ReportsDataBean>> entry : datalist.entrySet()) {

			((ObjectNode) rootNode).putArray(entry.getKey());
			ArrayNode arrayNode = (ArrayNode) rootNode.get(entry.getKey());
			ArrayList<ReportsDataBean> reportInfo = entry.getValue();
			for (ReportsDataBean data : reportInfo) {
				// Declare a tempNode for the element
				JsonNode tempNode = arrayNode.addObject();

				((ObjectNode) tempNode).put("name", data.getFileName());
				((ObjectNode) tempNode).put("uri", data.getFullPath());
				((ObjectNode) tempNode).put("timestamp", data.getTimeStamp());
			}

		}

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

	private String encrypt(String originalInput) {
		String encodedString = Base64.getEncoder().encodeToString(originalInput.getBytes());
		return encodedString;
	}

}
