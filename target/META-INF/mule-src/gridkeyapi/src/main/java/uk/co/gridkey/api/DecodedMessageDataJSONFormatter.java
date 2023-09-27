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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;

import com.google.gson.Gson;

import uk.co.gridkey.datacentre.AbstractJSONFormatter;
import uk.co.gridkey.datacentre.SimpleDateTimeContainer;
import uk.co.gridkey.db.CassandraConnector;
import uk.co.gridkey.db.CassandraSchemaMCU520;

public class DecodedMessageDataJSONFormatter extends AbstractJSONFormatter  {
	/**
	 * DecodedDataItem is a custom class used with GSON to easily take data and
	 * parse it into a json string to return via the API
	 */
	public class DecodedDataItem {
		String dno;
		String mcu;
		String start;
		String end;

		ArrayList<HashMap<String, String>> data;
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
	 * @param start
	 *            Start timestamp for the purposes of populating the JSON header and
	 *            identifying the data range contained within it
	 * @param end
	 *            End timestamp for the purposes of populating the JSON header and
	 *            identifying the data range contained within it
	 * @return String containing the JSON representation of the payload data
	 *         returned from the Cassandra database
	 */
	private HashMap<String, String>[] GenerateJSON(Map<String, Map<DateTime, Map<String, Double>>> retrievedData,
			String dno, String mcu, String start, String end) {
		// Create the HashMap array used to store each timestamped decoded data point
		@SuppressWarnings("unchecked")
		HashMap<String, String>[] dayArray = new HashMap[1440];

		// Loop through each parameter in the map
		for (Map.Entry<String, Map<DateTime, Map<String, Double>>> parameterData : retrievedData.entrySet()) {
			String parameterName = parameterData.getKey();
			// Loop through each days worth of data within the value map
			for (Map.Entry<DateTime, Map<String, Double>> dayData : parameterData.getValue().entrySet()) {
				// Get the timestamp from the key (this won't contain any hour/minute/second
				// info)
				DateTime timestamp = dayData.getKey();

				// Loop through each set of data
				for (Map.Entry<String, Double> keyValuePair : dayData.getValue().entrySet()) {
					// Recreate the date and time from the timestamp and secondsSinceMidnight
					// fields.
					SimpleDateTimeContainer dateTime = GenerateDateTimeString(timestamp, keyValuePair.getKey());

					// Get the array position based on the seconds since midnight
					int arrayPosition = Integer.valueOf(keyValuePair.getKey()) / 60;

					// Ensure we aren't going to get an out of bounds exception
					if (dayArray.length > arrayPosition) {
						// Check that the hashmap for the timestamp has been initialised populated
						if (dayArray[arrayPosition] == null) {
							// If it's not, we should initialise the hashmap and store the datetime
							dayArray[arrayPosition] = new HashMap<String, String>();
							dayArray[arrayPosition].put("datetime", dateTime.getDateTime());
						}

						// Store the item of data in the dayArray
						dayArray[arrayPosition].put(parameterName, Double.toString(keyValuePair.getValue()));
					}
				}
			}
		}

		// Pass back the days' worth of data
		return dayArray;
	}

	public Object onCall(Map<String, String> uriAttributes, Map<Object, Object> cassConnection) throws Exception {
		
		ArrayList<HashMap<String, String>> jsonData = new ArrayList<HashMap<String, String>>();

		// Get the data that was returned from the database query
		// Get the fields, which will be used to generate the JSON data
		
		String dno = uriAttributes.get("dno");
		String serialNumber = uriAttributes.get("mcu");
		String start = uriAttributes.get("start");
		String end = uriAttributes.get("end");
		String params = uriAttributes.get("params");
		
		// Get the cassandra connector and get the details required
		CassandraConnector cassConn =  (CassandraConnector) cassConnection.get("cassConn");
		CassandraSchemaMCU520 schema = new CassandraSchemaMCU520(cassConn);

		// Get the data from the database
		// Currently, the database only allows you to get all data or a single data
		// point
		// therefore we should split params and iterate over it and request the data for
		// each point individually.
		Map<String, Map<DateTime, Map<String, Double>>> result = new HashMap<String, Map<DateTime, Map<String, Double>>>();

		// Get the start and end dates as JodaTime objects - we need this to iterate
		// over each day
		DateTime startDate = TimeConverter.GridTimeToJodaTime(start);
		DateTime endDate = TimeConverter.GridTimeToJodaTime(end);

		// Get data for each day in the request
		for (DateTime date = startDate; date.isEqual(endDate) || date.isBefore(endDate); date = date.plusDays(1)) {
			for (String param : params.split(",")) {
				// Request the data for the point and add it to the result
				result.putAll(schema.getDecodedDataForPeriod(dno, serialNumber, date.getYearOfCentury(),
						date.getMonthOfYear(), date.getDayOfMonth(), date.getMonthOfYear(), date.getDayOfMonth(),
						param));
			}

			// Add the hashmap array to the list of data which will get returned as json
			jsonData.addAll(Arrays.asList(GenerateJSON(result, dno, serialNumber, start, end)));

			// Clear down the result array
			result.clear();
		}

		// Remove any null values from the list
		jsonData.removeAll(Collections.singleton(null));

		// Create the DecodedDataItem class with the
		DecodedDataItem decodedData = new DecodedDataItem();
		decodedData.dno = dno;
		decodedData.mcu = serialNumber;
		decodedData.start = start;
		decodedData.end = end;
		decodedData.data = jsonData;

		// Create the Gson object and get a json string based on the DecodedDataItem
		Gson gson = new Gson();
		return gson.toJson(decodedData);
	}
}