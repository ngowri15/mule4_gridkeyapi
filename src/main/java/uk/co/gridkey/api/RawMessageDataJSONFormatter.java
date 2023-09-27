//*****************************************************************************
// Name            : Raw Message Data JSON Formatter
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
// Title                     : RawMessageDataJSONFormatter.java
//
// Author                    : S. Brady, J. Griffiths
//
// Related Documents         : AP50077356 New Data Centre Database SDP
//
//*****************************************************************************
// Description
// -----------
// This file contains the class that formats the raw message data retrieved 
// from the database into JSON. Can be used to decode any raw message, but it
// should not be used to process data retrieved from the LATEST CONFIG column
// family, as other classes exist for this purpose. It is designed to be 
// invoked from a Mule flow.
// The class is designed to operate on a single day at a time. However it will
// format all data within a single day.
//
//*****************************************************************************

package uk.co.gridkey.api;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;

import uk.co.gridkey.datacentre.AbstractJSONFormatter;
import uk.co.gridkey.db.CassandraConnector;
import uk.co.gridkey.db.CassandraSchemaMCU520;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RawMessageDataJSONFormatter extends AbstractJSONFormatter {
	/**
	 * @param rawMsgBytesList
	 *            List of raw message bytes retrieved from the database
	 * @param dno
	 *            DNO identity
	 * @param mcu
	 *            MCU serial number
	 * @param message-type
	 *            Type of message that has been retrieved
	 * @param date
	 *            The data of the raw message data extracted
	 * @return String containing the JSON representation of the raw message bytes
	 */
	private String GenerateJSON(Map<String, Map<DateTime, Map<String, ByteBuffer>>> retrievedData, String dno,
			String mcu, String messageType, String date) {
		// Create a new ObjectMapper and set the rootNode to a new objectnode
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();

		// The fist elements in the JSON root are fixed format to help when determining
		// what the data represents
		((ObjectNode) rootNode).put("dno", dno);
		((ObjectNode) rootNode).put("mcu", mcu);
		((ObjectNode) rootNode).put("type", messageType);
		((ObjectNode) rootNode).put("date", date);
		((ObjectNode) rootNode).putArray("data");

		// Following on from the fixed format data is the array containing an entry for
		// each item in the List of columns
		// that were extracted from the database. Each of these entries contains the hex
		// representation of the raw message
		// data.
		ArrayNode arrayNode = (ArrayNode) rootNode.get("data");

		// Loop through each parameter in the map
		for (Map.Entry<String, Map<DateTime, Map<String, ByteBuffer>>> parameterData : retrievedData.entrySet()) {
			// Loop through each days worth of data within the value map
			for (Map.Entry<DateTime, Map<String, ByteBuffer>> dayData : parameterData.getValue().entrySet()) {
				// Loop through each set of data
				for (Map.Entry<String, ByteBuffer> keyValuePair : dayData.getValue().entrySet()) {
					// Get the bytes from the ByteBuffer and add convert it to a Hex string
					ByteBuffer tempByteBuffer = keyValuePair.getValue();
					byte[] bytes = new byte[tempByteBuffer.remaining()];
					tempByteBuffer.get(bytes);
					arrayNode.add(bytesToHex(bytes));
				}
			}
		}

		// Convert the rootNode to a string before returning it
		return rootNode.toString();
	}

	public Object onCall(Map<String, String> uriAttributes, Map<Object, Object> cassConnection) throws Exception {
		// Set jsonData to an empty JSON node initially
		String jsonData = "{}";

		// Get the data that was returned from the database query
		// Get the parameters supplied in the URI, such as MCU serial number, dno and
		// measurement type
		String dno = uriAttributes.get("dno");
		String mcu = uriAttributes.get("mcu");
		String serialNumber = uriAttributes.get("mcu");
		String date = uriAttributes.get("date");
		String messageType = uriAttributes.get("type");
		
		// Get the cassandra connector and get the details required
		CassandraConnector cassConn =  (CassandraConnector) cassConnection.get("cassConn");
		CassandraSchemaMCU520 schema = new CassandraSchemaMCU520(cassConn);

		// Change the message type to be an actual message ID based upon what it is
		// currently set to
		switch (messageType) {
		case "statistical": {
			// Set to statistical message ID, as per ICD definition
			messageType = "E1D2C3B480010000";
		}
			break;

		case "instantaneous": {
			// Set to instantaneous message ID, as per ICD definition
			messageType = "E1D2C3B480010200";
		}
			break;

		case "alert": {
			// Alert ID, as per ICD definition
			messageType = "E1D2C3B480020000";
		}
			break;

		case "status": {
			// BIT status ID, as per ICD definition
			messageType = "E1D2C3B480110000";
		}
			break;

		case "factory": {
			// Factory ID, as per ICD definition
			messageType = "E1D2C3B480050000";
		}
			break;

		case "user": {
			// BIT status ID, as per ICD definition
			messageType = "E1D2C3B480050100";
		}
			break;

		case "calibration": {
			// Calibration ID, as per ICD definition
			messageType = "E1D2C3B480050300";
		}
			break;

		default: {
			// Leave message-type as it is. As a result of this the database will fail to
			// retrieve the row key if it doesn't exist, or
			// during debug etc it will be able to return data without having to update the
			// API code.
		}
		}

		// Get the data from the database
		// Currently, the database only allows you to get all data or a single data
		// point
		// therefore we should split params and iterate over it and request the data for
		// each point individually.
		Map<String, Map<DateTime, Map<String, ByteBuffer>>> result = new HashMap<String, Map<DateTime, Map<String, ByteBuffer>>>();

		// Request the data for the point and add it to the result
		result.putAll(schema.getRawDataForDay(dno, serialNumber, Integer.valueOf(date.substring(0, 2)),
				Integer.valueOf(date.substring(2, 4)), Integer.valueOf(date.substring(4, 6)), messageType));

		// Format the data as JSON and pass back to the mule flow
		jsonData = GenerateJSON(result, dno, mcu, messageType, date);
		return jsonData;
	}
}
