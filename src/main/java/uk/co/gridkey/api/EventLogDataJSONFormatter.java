//*****************************************************************************
// Name            : Event Log Data JSON Formatter
//
// Security Classification   : UNCLASSIFIED
//
// Copyright(s)              :
//
// The copyright in this document is the property of Lucy Electric. The document
// is supplied by Lucy Electric on the express understanding that it is to be
// treated as confidential and that it may not be copied, used or disclosed to
// others in whole or in part for any purpose except as authorised in writing
// by Lucy Electric.
//
// Unless Lucy Electric has accepted a contractual obligation in respect of the
// permitted use of the information and data contained herein such information
// and data is provided without responsibility and Lucy Electric disclaims all
// liability arising from its use.
//
//*****************************************************************************
// Project                   : LVSMS
//
// Title                     : EventLogDataJSONFormatter.java
//
// Author                    : S. Brady
//
// Related Documents         : 
//
//*****************************************************************************
// Description
// -----------
// This file contains the class that is responsible for formatting any event
// log data into JSON. It is designed to be invoked from a Mule flow.
//
//*****************************************************************************

package uk.co.gridkey.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.co.gridkey.datacentre.AbstractJSONFormatter;
import uk.co.gridkey.db.CassandraConnector;
import uk.co.gridkey.db.CassandraSchemaMCU520;
import uk.co.gridkey.db.udt.EventLogUDT;
import uk.co.gridkey.db.udt.EventLogUDT.EventLogType;

public class EventLogDataJSONFormatter extends AbstractJSONFormatter {
	/**
	 * EventLogDataItem is a custom class used with GSON to easily take data and
	 * parse it into a json string to return via the API
	 */
	protected class EventLogDataItem {
		String dno;
		String mcu;
		String start;
		String end;

		ArrayList<HashMap<String, ArrayList<HashMap<String, String>>>> data;
	}

	/**
	 * Generates the JSON formatted string representing the event log data retrieved
	 * from the Cassandra database.
	 * 
	 * @param retrievedData
	 *            Decoded payload data from the Cassandra database
	 * @param dno
	 *            DNO identity
	 * @param mcu
	 *            MCU serial number
	 * @param type
	 *            The type of event specified
	 * @param startDate
	 *            Start timestamp for the purposes of populating the JSON header and
	 *            identifying the data range contained within it
	 * @param endDate
	 *            End timestamp for the purposes of populating the JSON header and
	 *            identifying the data range contained within it
	 * @return String containing the JSON representation of the event log data
	 *         returned from the Cassandra database
	 */
	private JsonNode GenerateJSONRootNode(String dno, String mcu, String type, String startDate, String endDate) {
		// Create a new ObjectMapper and set the rootNode to a new objectnode
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();

		// The fist elements in the JSON root are fixed format to help when determining
		// what the data represents
		((ObjectNode) rootNode).put("dno", dno);
		((ObjectNode) rootNode).put("mcu", mcu);
		((ObjectNode) rootNode).put("type", type);
		((ObjectNode) rootNode).put("start", startDate);
		((ObjectNode) rootNode).put("end", endDate);
		((ObjectNode) rootNode).putArray("events");

		return rootNode;
	}

	private void AddJSONEventNodes(Map<String, Map<DateTime, Map<String, EventLogUDT>>> retrievedData,
			ArrayNode eventsArray) {
		// Loop through each parameter in the map
		for (Map.Entry<String, Map<DateTime, Map<String, EventLogUDT>>> parameterData : retrievedData.entrySet()) {
			// Attempt to get the ArrayNode for the given parameter
			ArrayNode eventArrayNode = (ArrayNode) eventsArray.findValue(parameterData.getKey());

			// If the ArrayNode is null, one hasn't been created before.
			// Create it now and add a new array
			if (eventArrayNode == null) {
				JsonNode parameterNode = eventsArray.addObject();
				eventArrayNode = ((ObjectNode) parameterNode).putArray(parameterData.getKey());
			}

			// Loop through each days worth of data within the value map
			for (Map.Entry<DateTime, Map<String, EventLogUDT>> dayData : parameterData.getValue().entrySet()) {
				// Loop through each set of event data in the map
				for (Map.Entry<String, EventLogUDT> keyValuePair : dayData.getValue().entrySet()) {
					JsonNode tempEvent = eventArrayNode.addObject();
					((ObjectNode) tempEvent).put("date-time",
							GenerateDateTimeString(dayData.getKey(), keyValuePair.getKey()).getDateTime());
					((ObjectNode) tempEvent).put("sub-type", keyValuePair.getValue().getEventLogSubType());
					((ObjectNode) tempEvent).put("comments", keyValuePair.getValue().getEventLogComments());
					((ObjectNode) tempEvent).put("parameter-list", keyValuePair.getValue().getEventLogParameterList());
					((ObjectNode) tempEvent).put("state", keyValuePair.getValue().getEventLogEntryState());
					((ObjectNode) tempEvent).put("value", keyValuePair.getValue().getEventLogValue());
				}
			}
		}
	}

	public Object onCall(Map<String, String> uriAttributes, Map<Object, Object> cassConnection) throws Exception {
		// Get the data that was returned from the database query

		// Get the fields, which will be used to generate the JSON data
		String dno = uriAttributes.get("dno");
		String serialNumber = uriAttributes.get("mcu");
		String start = uriAttributes.get("start");
		String end = uriAttributes.get("end");
		String typeString = uriAttributes.get("type");
		String params;
		if(uriAttributes.get("params")==null) {
			params=null;
		}
		else {
			params = uriAttributes.get("params");
		}		
		EventLogType eventLogType = EventLogType.ANALYTIC; // Set to anything by default
		Map<String, Map<DateTime, Map<String, EventLogUDT>>> result = new HashMap<String, Map<DateTime, Map<String, EventLogUDT>>>();

		CassandraConnector cassConn =  (CassandraConnector) cassConnection.get("cassConn");
		CassandraSchemaMCU520 schema = new CassandraSchemaMCU520(cassConn);

		// Translate the request to a known event log type (add more as others are used)
		if (typeString.contains("analytic")) {
			eventLogType = EventLogType.ANALYTIC;
		} else if (typeString.contains("alert")) {
			eventLogType = EventLogType.ALERT;
		}

		JsonNode rootNode = GenerateJSONRootNode(dno, serialNumber, typeString, start, end);
		ArrayNode eventsArray = (ArrayNode) rootNode.get("events");

		// Get the start and end dates as JodaTime objects - we need this to iterate
		// over each day
		DateTime startDate = TimeConverter.GridTimeToJodaTime(start);
		DateTime endDate = TimeConverter.GridTimeToJodaTime(end);

		// Currently, the database only allows you to get all data or a single data
		// point per day, therefore
		// split params and iterate over it and request the data for each point
		// individually.
		for (DateTime date = startDate; date.isEqual(endDate) || date.isBefore(endDate); date = date.plusDays(1)) {
			if (params != null) {
				for (String param : params.split(",")) {
					// Request the data for the event parameter and add it to the result
					result.putAll(schema.getEventLogDataForPeriod(dno, serialNumber, date.getYearOfCentury(),
							date.getMonthOfYear(), date.getDayOfMonth(), date.getMonthOfYear(), date.getDayOfMonth(),
							eventLogType.name(), param));
				}
			} else {
				// No parameters, so just get everything
				// Request the data for the event parameter and add it to the result
				result = schema.getEventLogDataForDayAllParams(dno, serialNumber, date.getYearOfCentury(),
						date.getMonthOfYear(), date.getDayOfMonth(), eventLogType.name());
			}

			AddJSONEventNodes(result, eventsArray);
		}

		return rootNode.toString();
	}
}