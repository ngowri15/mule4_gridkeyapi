//*****************************************************************************
// Name            : Latest Message Data JSON Formatter
//
// Security Classification   : UNCLASSIFIED
//
// Copyright(s)              :
//
// The copyright in this document is the property of SELEX-ES. The document
// is supplied by SELEX-ES on the express understanding that it is to be
// treated as confidential and that it may not be copied, used or disclosed to
// others in whole or in part for any purpose except as authorised in writing
// by SELEX-ES.
//
// Unless SELEX-ES has accepted a contractual obligation in respect of the
// permitted use of the information and data contained herein such information
// and data is provided without responsibility and SELEX-ES disclaims all
// liability arising from its use.
//
//*****************************************************************************
// Project                   : LVSMS
//
// Title                     : ConfigDataJSONFormatter.java
//
// Author                    : S. Brady, J. Griffiths
//
// Related Documents         : AP50077356 New Data Centre Database SDP
//
//*****************************************************************************
// Description
// -----------
// This file contains the class that is responsible for generating the JSON 
// formatted string for latest message data. It is designed to be called from
// a Mule flow directly.
//
//*****************************************************************************

package uk.co.gridkey.api;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import uk.co.gridkey.datacentre.AbstractJSONFormatter;
import uk.co.gridkey.datacentre.PayloadDataDecoder;

public class LatestMessageJSONFormatter extends AbstractJSONFormatter {
	/**
	 * Generates the JSON formatted string that is returned by the API. This string
	 * contains the latest message data in JSON format.
	 * 
	 * @param decodedPayloadMap
	 *            Map of decoded parameters from a decoded Gridkey MCU message
	 * @param dno
	 *            DNO identity
	 * @param mcu
	 *            MCU serial number
	 * @param config-type
	 *            The type of message being generated
	 * @param msgTimestamp
	 *            The requested timestamp
	 * @return JSON formatted string containing the latest message data
	 */
	private String GenerateJSON(Map<String, String> decodedPayloadMap, String dno, String mcu, String configType,
			String msgTimestamp) {
		// Create a new ObjectMapper and create a new root node
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();

		// The fist elements in the JSON root are fixed format to help user of the API
		// determine what the data represents
		((ObjectNode) rootNode).put("dno", dno);
		((ObjectNode) rootNode).put("mcu", mcu);
		((ObjectNode) rootNode).put("type", configType);
		((ObjectNode) rootNode).putObject("data");

		// Under the data array place each item in the payload map as a single entry,
		// starting with the message timestamp which
		// is not in the decoded payload map
		JsonNode tempNode = rootNode.get("data");
		((ObjectNode) tempNode).put("datetime", msgTimestamp);

		// Sort the decoded payload map and iterate through adding each item to the node
		SortedSet<String> keys = new TreeSet<String>(decodedPayloadMap.keySet());
		for (String key : keys) {
			// Put the key and value into the relevant node
			((ObjectNode) tempNode).put(key, decodedPayloadMap.get(key));
		}

		// Convert the rootNode to a string before returning it
		return rootNode.toString();
	}

	public Object onCall(Map<Object, Object> latestMessageExtractQueryContents, String jsonFileCacheManager, Map<String, String> uriAttributes) throws Exception {
		// Set jsonData to an empty JSON node initially
		String jsonData = "{}";
		
		// Get the data that was returned from the database query

		// Get the cached JSON format description file

		// Only process data if rawMsgBytes is not null - i.e. we actually retrieved
		// data and the JSON
		// file exists
		
		byte[] rawMsgBytes = (byte[]) latestMessageExtractQueryContents.get("rawMsgBytes");
		String json = jsonFileCacheManager;
		String dno = uriAttributes.get("dno");
		String mcu = uriAttributes.get("mcu");
		String messageType = uriAttributes.get("type");
		
		if (rawMsgBytes != null && !json.isEmpty()) {
			// Get the parameters supplied in the URI, such as MCU serial number, dno and
			// config-type
			
			// Extract the date time from the message common header
			String msgTimestamp = ExtractMsgTimestamp(rawMsgBytes);

			// Decode the payload data contained within the config message. Traverse KLV
			// returns a map of parameter names (keys)
			// against their string representation (values).
			PayloadDataDecoder dataDecoder = new PayloadDataDecoder();
			Map<String, String> decodedPayloadMap = dataDecoder.traverseKLV(rawMsgBytes, json);

			// Format the data as JSON and pass back to the mule flow
			jsonData = GenerateJSON(decodedPayloadMap, dno, mcu, messageType, msgTimestamp);
		}
		return jsonData;
	}
}
