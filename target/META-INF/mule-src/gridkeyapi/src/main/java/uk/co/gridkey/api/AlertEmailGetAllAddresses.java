//*****************************************************************************
// Name            : Alert Email Get All Addresses
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
// Title                     : AlertEmailGetAddresses.java
//
// Author                    : J. Griffiths
//
// Related Documents         : AP50077356 New Data Centre Database SDP
//
//*****************************************************************************
// Description
// -----------
// This file retrieves a list of the email addresses associated with the given
// DNO.
//
//*****************************************************************************

package uk.co.gridkey.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import uk.co.gridkey.db.CassandraConnector;
import uk.co.gridkey.db.CassandraSchemaMCU520;

public class AlertEmailGetAllAddresses {

	/**
	 * Generates the JSON formatted string containing the list of email addresses
	 * retrieved from the Cassandra database.
	 * 
	 * @param dno
	 *            DNO identity
	 * @param emailAddresses
	 *            List of email addresses
	 * @return String containing the JSON representation of the list of alert email
	 *         addresses returned from the Cassandra database
	 */
	private String GenerateJSON(String dno, List<String> emailAddresses) {
		// Create a new ObjectMapper and set the rootNode to a new objectnode
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();

		// The fist elements in the JSON root are fixed format to help when determining
		// what the data represents
		((ObjectNode) rootNode).put("dno", dno);

		// Create a node object for the email addresses array
		ArrayNode AlertEmailArray = ((ObjectNode) rootNode).putArray("emailaddresses");

		// Add each of the email addresses to the array
		for (String emailAddress : emailAddresses) {
			AlertEmailArray.add(emailAddress);
		}

		// Convert the rootNode to a string before returning it
		return rootNode.toString();
	}

	public Object onCall(Map<String, String> uriAttributes, Map<Object, Object> cassConnection) throws Exception {

		Map<Object, Object> msg = new HashMap<Object, Object>();

		// Get the flow message

		// Extract the required properties
		String dno = uriAttributes.get("dno");

		// Get the Cassandra connector and create a new schema
		CassandraConnector cassConn =  (CassandraConnector) cassConnection.get("cassConn");
		CassandraSchemaMCU520 schema = new CassandraSchemaMCU520(cassConn);

		List<String> emailAddresses = schema.getAlertEmailAddresses(dno);

		msg.put("response", "{\"message\": \"success\"}");
		msg.put("responseCode", 200);
		msg.put("payload",GenerateJSON(dno, emailAddresses));
		return msg;
	}
}
