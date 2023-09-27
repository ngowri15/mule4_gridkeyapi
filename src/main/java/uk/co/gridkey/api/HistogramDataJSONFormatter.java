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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.joda.time.DateTime;

import uk.co.gridkey.datacentre.AbstractJSONFormatter;
import uk.co.gridkey.datacentre.SimpleDateTimeContainer;
import uk.co.gridkey.db.CassandraConnector;
import uk.co.gridkey.db.CassandraSchemaMCU520;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class HistogramDataJSONFormatter extends AbstractJSONFormatter {
	/**
	 * Determines what the correct array node is in the JSON structure being
	 * constructed in memory based upon the datetime string passed in. If a node
	 * matches the datatime string supplied, then that node is returned. Otherwise a
	 * new node is added with the datatime as the field name, and that node is
	 * returned instead.
	 * 
	 * @param arrayNode
	 * @param dateTimeString
	 *            in the format YYMMDDhhmmss
	 * @return The JSON node that represents the datatime string requested
	 */
	private JsonNode GetCorrectArrayNodeObject(ArrayNode arrayNode, String dateTimeString) {
		int index = 0;
		JsonNode tempNode = null;
		boolean nodeFound = false;

		// Go through all of the array nodes until no more nodes exist, or the correct
		// node has been found.
		while ((index < arrayNode.size()) && !nodeFound) {
			tempNode = arrayNode.get(index);

			// See if the field exists within the JSON node
			Iterator<String> fieldNames = tempNode.fieldNames();
			while (fieldNames.hasNext()) {
				String fieldName = fieldNames.next();
				if (fieldName.contentEquals("Field")) {
					JsonNode fieldValue = tempNode.get(fieldName);
					String value = fieldValue.asText();

					if (value.contentEquals(dateTimeString)) {
						// Field exists, therefore the node has been found
						nodeFound = true;
					}
				}
			}

			index++;
		}

		// If we have gone through all objects in the array node and the correct node
		// was not found, add a new one
		// and add a datetime field
		if (!nodeFound) {
			tempNode = arrayNode.addObject();
			((ObjectNode) tempNode).put("Field", dateTimeString);
		}

		// At this point a new node or the node that was found will be returned
		return tempNode;
	}

	private String HistogramToProfileCurve(String data) {
		String[] tempBins = data.split(",");

		// Take the string representation and convert to an array
		double[] histBins = new double[tempBins.length];
		double[] profile = new double[tempBins.length];
		double sum = 0;

		for (int i = 0; i < tempBins.length; i++) {
			histBins[i] = Double.valueOf(tempBins[i]);
			sum += histBins[i];
		}

		// Normalise the data
		profile[0] = histBins[0] / sum;

		for (int i = 1; i < histBins.length; i++) {
			profile[i] = profile[i - 1] + (histBins[i] / sum);
		}

		// Convert the data back to a single comma separated string, removing any
		// additional
		// brackets that Arrays adds
		String profileString = Arrays.toString(profile);
		return profileString.substring(1, profileString.length() - 2);
	}

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
	 * @param date
	 *            Timestamp for the purposes of populating the JSON header and
	 *            identifying the data range contained within it
	 * @return String containing the JSON representation of the payload data
	 *         returned from the Cassandra database
	 */
	private String GenerateJSON(Map<String, Map<DateTime, Map<String, String>>> retrievedData, String dno, String mcu,
			String date, String histogramType) {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();

		// The fist elements in the JSON root are fixed format to help when determining
		// what the data represents
		((ObjectNode) rootNode).put("dno", dno);
		((ObjectNode) rootNode).put("mcu", mcu);
		((ObjectNode) rootNode).put("date", date);
		((ObjectNode) rootNode).putArray("data");

		// Following on from the fixed format data is the array containing an entry for
		// each time period retrieved between
		// the start and end times that were specified when retrieving the data. Inside
		// each of these time period fields
		// are the individual parameters names and their associated values e.g. "range",
		// "(200.0, 300.0) in 201"
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
					// fields. Format is
					// YYMMDDhhmmss.
					SimpleDateTimeContainer dateTime = GenerateDateTimeString(timestamp, keyValuePair.getKey());

					// Locate the correct node to insert this data into - if it doesn't exist, the
					// function will create it and return it
					JsonNode arrayNodeObject = GetCorrectArrayNodeObject(arrayNode, dateTime.getDateTime());

					if (arrayNodeObject != null) {
						// Finally add a new field to the node, storing column data against column name.
						// If the field is
						// 'data', then the histogram type needs to be taken into account to create a
						// profile curve or
						// histogram
						if (histogramType.equals("profile")) {
							((ObjectNode) arrayNodeObject).put(parameterName,
									HistogramToProfileCurve(keyValuePair.getValue()));
						} else {
							((ObjectNode) arrayNodeObject).put(parameterName.replace("Duration Curve", "Histogram"),
									keyValuePair.getValue());
						}
					}
				}
			}
		}

		// Pass back a string representation of the JSON
		return rootNode.toString();
	}

	public Object onCall(Map<String, String> uriAttributes, Map<Object, Object> cassConnection) throws Exception {
		String jsonData = "{}";

		// Get the data that was returned from the database query
		// Get the fields, which will be used to generate the JSON data
		String dno = uriAttributes.get("dno");
		String serialNumber = uriAttributes.get("mcu");
		String histogramType = uriAttributes.get("type");
		String date = uriAttributes.get("date");
		
		CassandraConnector cassConn =  (CassandraConnector) cassConnection.get("cassConn");
		CassandraSchemaMCU520 schema = new CassandraSchemaMCU520(cassConn);

		// Get the data from the database
		// Currently, the database only allows you to get all data or a single data
		// point
		// therefore we should split params and iterate over it and request the data for
		// each point individually.
		Map<String, Map<DateTime, Map<String, String>>> result = new HashMap<String, Map<DateTime, Map<String, String>>>();

		for (String param : histogramType.split(",")) {
			// Request the data for the point and add it to the result
			result.putAll(schema.getVisualisationDataForPeriod(dno, serialNumber, Integer.valueOf(date.substring(0, 2)),
					Integer.valueOf(date.substring(2, 4)), Integer.valueOf(date.substring(4, 6)),
					Integer.valueOf(date.substring(2, 4)), Integer.valueOf(date.substring(4, 6)), param));
		}

		// Format the data as JSON and pass back to the mule flow
		jsonData = GenerateJSON(result, dno, serialNumber, date, histogramType);

		return jsonData;
	}
}
