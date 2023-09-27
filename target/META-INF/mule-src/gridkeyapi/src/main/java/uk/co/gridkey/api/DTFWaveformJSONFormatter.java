//*****************************************************************************
// Name            : Decoded Message Data JSON Formatter
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
// Title                     : DecodedMessageDataJSONFormatter.java
//
// Author                    : S. Brady, J. Griffiths
//
// Related Documents         : AP50077356 New Data Centre Database SDP
//
//*****************************************************************************
// Description
// -----------
// This file contains the class that is responsible for formatting any decoded
// message data into JSON. It is designed to be invoked from a Mule flow.
//
//*****************************************************************************

package uk.co.gridkey.api;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;

import org.joda.time.DateTime;

import uk.co.gridkey.datacentre.AbstractJSONFormatter;
import uk.co.gridkey.datacentre.SimpleDateTimeContainer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DTFWaveformJSONFormatter extends AbstractJSONFormatter {
	/**
	 * Generates the JSON formatted string representing the decoded message data
	 * retrieved from the Cassandra database.
	 * 
	 * @param retrievedData
	 *            Decoded payload data from the Cassandra database
	 * @param dno
	 *            DNO identity
	 * @param mcu
	 *            MCU serial number
	 * @param start
	 *            Start timestamp for the purposes of populating the JSON header and
	 *            identifying the data range contained within it
	 * @param end
	 *            End timestamp for the purposes of populating the JSON header and
	 *            identifying the data range contained within it
	 * @return String containing the JSON representation of the payload data
	 *         returned from the Cassandra database
	 */
	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	private String GenerateJSON(Map<String, Map<DateTime, Map<String, String>>> retrievedData, String dno, String mcu,
			String date) {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();

		// The fist elements in the JSON root are fixed format to help when determining
		// what the data represents
		((ObjectNode) rootNode).put("dno", dno);
		((ObjectNode) rootNode).put("mcu", mcu);
		((ObjectNode) rootNode).putArray("data");

		// Following on from the fixed format data is the array containing an entry for
		// each time period retrieved between
		// the start and end times that were specified when retrieving the data. Inside
		// each of these time period fields
		// are the individual parameters names and their associated values e.g.
		// "v1-dtf-waveform", "0.1, 0.5, ..."
		ArrayNode arrayNode = (ArrayNode) rootNode.get("data");

		// Loop through each parameter in the map
		for (Map.Entry<String, Map<DateTime, Map<String, String>>> parameterData : retrievedData.entrySet()) {
			String parameterName = parameterData.getKey();
			// Loop through each days worth of data within the value map
			for (Map.Entry<DateTime, Map<String, String>> dayData : parameterData.getValue().entrySet()) {
				DateTime timestamp = dayData.getKey();
				// Loop through each set of data
				for (Map.Entry<String, String> keyValuePair : dayData.getValue().entrySet()) {
					// Recreate the date and time from the timestamp and secondsSinceMidnight
					// fields.
					SimpleDateTimeContainer dateTime = GenerateDateTimeString(timestamp, keyValuePair.getKey());

					// Locate the correct node to insert this data into - if it doesn't exist, the
					// function will create it and return it
					// JsonNode arrayNodeObject = GetCorrectArrayNodeObject(arrayNode,
					// dateTimeField);

					// if (arrayNodeObject != null)
					// {
					// Finally add a new field to the node, storing column data against column name.
					// If the field is
					// 'data', then the histogram type needs to be taken into account to create a
					// profile curve or
					// histogram
					// if (type.equals("profile"))
					// {
					// ((ObjectNode)arrayNodeObject).put(parameterName,
					// HistogramToProfileCurve(keyValuePair.getValue()));
					// }
					// else
					// {
					// ((ObjectNode)arrayNodeObject).put(parameterName.replace("duration-curve",
					// "histogram"), keyValuePair.getValue());
					// }
				}
			}
		}

		// For each item in the received data map, extract the row key and insert the
		// data into the "data" array nodes
		Iterator element = retrievedData.entrySet().iterator();
		while (element.hasNext()) {
			// Take the key value pair for each. The key contains the row key, and the value
			// contains
			// the list of columns that were extracted from the database for each given key
			Map.Entry pairs = (Map.Entry) element.next();
			// List<ColumnOrSuperColumn> columnList = (List<ColumnOrSuperColumn>)
			// pairs.getValue();

			// Define a new temporary byte array and get the bytes from the key. Note the
			// ByteBuffer has it's Position and Limit set,
			// we just need to get the remaining bytes from this position up to the end of
			// the array
			ByteBuffer key = (ByteBuffer) pairs.getKey();
			byte[] tempByteArray = new byte[key.remaining()];
			key.get(tempByteArray);

			// Convert the bytes to a row key string
			String rowKey = new String(tempByteArray);

			int index = 0;
			String columnValue = null;
			String secondsSinceMidnight = null;
			// while (index < columnList.size())
			// {
			// Extract the column name (seconds since midnight) and the value
			// ColumnOrSuperColumn columnObject = columnList.get(index++);
			// Column column = columnObject.getColumn();

			// Perform a split on colon. Alerts are stored in the format AAA:BBB
			// All data after (including the colon) can be discarded as this is purely
			// used to stop data being overwritten should multiple alerts be triggered
			// simultaneously

			// List<String> myList = Arrays.asList(new String(column.getName()).split(":"));
			// if (myList.size() != 0)
			// {
			// secondsSinceMidnight = myList.get(0);
			// }

			// Recreate the date and time from the row key and the column name fields.
			// Format is
			// YYMMDDhhmmss
			// String dateTimeField = GenerateDateTimeString(rowKey, secondsSinceMidnight,
			// date);

			// If row key is not null put it in a new JSON node - one shouldn't already
			// exist
			// when finding the correct one to put it in, so it will create one instead
			// JsonNode arrayNodeObject = null;
			// if (rowKey != null)
			// {
			// arrayNodeObject = arrayNode.addObject();
			// ((ObjectNode)arrayNodeObject).put("datetime", dateTimeField);
			// }

			// columnValue = new String(column.getValue());

			// if (dateTimeField != null)
			// {
			// if (arrayNodeObject != null)
			// {
			// Get the name of the field
			// String fieldName = ExtractFieldNameFromRowKey(rowKey);
			// ((ObjectNode)arrayNodeObject).put("field", fieldName);

			// Finally add a new field to the node, containing column name against row key.
			// However only add it if
			// the node object isn't null
			// ((ObjectNode)arrayNodeObject).put("data", columnValue);

			// Need to add the labels
			// String Labels = "";
			// for (int i = 1; i <= columnValue.split(",").length; i++) {
			// Labels += i + ",";
			// }
			// Labels = Labels.substring(0, Labels.length() - 1);
			// ((ObjectNode)arrayNodeObject).put("Labels", Labels);
			// }
			// }
			// }
		}

		// Pass back a string representation of the JSON
		return rootNode.toString();
	}

	public Object onCall(Map<String, String> uriAttributes) throws Exception {
		String jsonData = "{}";

		// Get the data that was returned from the database query
		// Map<ByteBuffer, List<ColumnOrSuperColumn>> retrievedData = (Map<ByteBuffer,
		// List<ColumnOrSuperColumn>>)msg.getPayload();
		///
		// Get the fields, which will be used to generate the JSON data
		String dno = uriAttributes.get("dno");
		String mcu = uriAttributes.get("mcu");
		String date = uriAttributes.get("date");

		// Format the data as JSON and pass back to the mule flow
		// jsonData = GenerateJSON(retrievedData, dno, mcu, date);

		return jsonData;
	}
}
