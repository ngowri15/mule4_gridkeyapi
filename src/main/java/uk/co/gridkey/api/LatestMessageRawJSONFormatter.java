//*****************************************************************************
// Name            : Config Data Raw JSON Formatter
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
// Title                     : ConfigDataRawJSONFormatter.java
//
// Author                    : S. Brady, J. Griffiths
//
// Related Documents         : AP50077356 New Data Centre Database SDP
//
//*****************************************************************************
// Description
// -----------
// This file contains the logic for formating raw binary data as JSON. The 
// raw binary data is converted into ascii readable form. It is specific to the
// configuration data, in particular processing the latest configs, as the 
// latest configs are stored in a different format to other raw binary 
// messages. This is designed to be invoked from a Mule flow.
//
//*****************************************************************************

package uk.co.gridkey.api;

import uk.co.gridkey.datacentre.AbstractJSONFormatter;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class LatestMessageRawJSONFormatter extends AbstractJSONFormatter {
	/**
	 * Generates the JSON string representation of the latest binary config data
	 * 
	 * @param rawMsgBytes
	 *            The raw binary message data from the LATEST CONFIGS column family
	 * @param dno
	 *            DNO identity
	 * @param mcu
	 *            MCU serial number
	 * @param config-type
	 *            Configuration message type
	 * @param msgTimestamp
	 *            The requested timestamp
	 * @return String JSON formatted representation of the raw binary data for the
	 *         requested configuration message
	 */
	private String GenerateJSON(byte[] rawMsgBytes, String dno, String mcu, String configType, String msgTimestamp) {
		// Create a new ObjectMapper and set the rootNode to a new objectnode
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();

		// The fist elements in the JSON root are fixed format to help when determining
		// what the data represents
		((ObjectNode) rootNode).put("dno", dno);
		((ObjectNode) rootNode).put("mcu", mcu);
		((ObjectNode) rootNode).put("type", configType);
		((ObjectNode) rootNode).put("datetime", msgTimestamp);
		((ObjectNode) rootNode).put("data", bytesToHex(rawMsgBytes));

		// Convert the rootNode to a string before returning it
		return rootNode.toString();
	}

	public Object onCall(Map<Object, Object> latestMessageExtractQueryContents, Map<String, String> uriAttributes) throws Exception {
		// Set jsonData to an empty JSON node initially
		String jsonData = "{}";

		// Get the data that was returned from the database query

		// Only process data if rawMsgBytes is not null - i.e. we actually retrieved
		// data
		
		byte[] rawMsgBytes = (byte[]) latestMessageExtractQueryContents.get("rawMsgBytes");
		String dno = uriAttributes.get("dno");
		String mcu = uriAttributes.get("mcu");
		String messageType = uriAttributes.get("type");
		
		if (rawMsgBytes != null) {
			// Get the parameters supplied in the URI, such as MCU serial number, dno and
			// config-type
			// Extract the date time from the message common header
			String msgTimestamp = ExtractMsgTimestamp(rawMsgBytes);

			// Format the data as JSON and pass back to the mule flow
			jsonData = GenerateJSON(rawMsgBytes, dno, mcu, messageType, msgTimestamp);
		}

		return jsonData;
	}
}
