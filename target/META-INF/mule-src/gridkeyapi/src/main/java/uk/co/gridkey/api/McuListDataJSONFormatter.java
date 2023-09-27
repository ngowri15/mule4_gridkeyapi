//*****************************************************************************
// Name            : Mcu List Data JSON Formatter
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
// Title                     : McuListDataJSONFormatter.java
//
// Author                    : S. Brady, J. Griffiths
//
// Related Documents         : AP50077356 New Data Centre Database SDP
//
//*****************************************************************************
// Description
// -----------
// This file contains the logic to generate a JSON formatted list of MCUs that
// exist within the Cassandra database. It is designed to be invoked from a
// Mule flow.
//
//*****************************************************************************

package uk.co.gridkey.api;

import java.util.List;
import java.util.Map;

import uk.co.gridkey.datacentre.AbstractJSONFormatter;
import uk.co.gridkey.db.CassandraConnector;
import uk.co.gridkey.db.CassandraSchemaGridKeyMCU;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class McuListDataJSONFormatter extends AbstractJSONFormatter {
	/**
	 * Generates the JSON formatted string of MCU serial numbers stored in the
	 * database.
	 * 
	 * @param retrievedData
	 *            The data retrieved from the database
	 * @param dno
	 *            DNO identity
	 * @return String containing the JSON formatted list of MCU serial numbers
	 * @throws JsonProcessingException
	 */
	private String GenerateJSON(List<Map<String, String>> retrievedData, String dno) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();

		// The fist elements in the JSON root are fixed format to help when determining
		// what the data represents
		((ObjectNode) rootNode).put("dno", dno);
		((ObjectNode) rootNode).putArray("mcus");

		ArrayNode arrayNode = (ArrayNode) rootNode.get("mcus");

		// For each item in the received data map, extract the row key and insert the
		// data into the "data" array nodes
		for (Map<String, String> mcuDetails : retrievedData) {
			// Declare a tempNode for the element
			JsonNode tempNode = arrayNode.addObject();

			// Loop through each pair in the Row Key
			for (Map.Entry<String, String> mapEntry : mcuDetails.entrySet()) {
				// Put each column into the tempNode
				((ObjectNode) tempNode).put(new String(mapEntry.getKey()), new String(mapEntry.getValue()));
			}
		}

		// Pass back a string representation of the JSON
		return convertNode(rootNode);
	}
	
	public Object onCall(Map<String, String> uriAttributes, Map<Object, Object> cassConnection) throws Exception {
		
		// Get the fields, which will be used to generate the JSON data
		String dno = uriAttributes.get("dno");
		
		// Get the cassandra connector and get the details required
		CassandraConnector cassConn =  (CassandraConnector) cassConnection.get("cassConn");
		CassandraSchemaGridKeyMCU schema = new CassandraSchemaGridKeyMCU(cassConn);
		
		System.out.println("CassandraSchemaGridKeyMCU schema created");
		List<Map<String, String>> retrievedData = schema.getMcuDetails(dno);

		// Format the data as JSON and pass back to the mule flow
		String jsonData = GenerateJSON(retrievedData, dno);
		//msg.setProperty("jsonData", jsonData, PropertyScope.INVOCATION);
		return jsonData;
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
