//*****************************************************************************
// Name            : Describe Keyspaces Decoder
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
// Title                     : DescribeKeyspacesDecoder.java
//
// Author                    : S. Brady, J. Griffiths
//
// Related Documents         : AP50077356 New Data Centre Database SDP
//
//*****************************************************************************
// Description
// -----------
// This file contains the logic to process the Cassandra query response and 
// extract a list of DNOs from the database. It is designed to be invoked from
// a Mule flow.
//
//*****************************************************************************

package uk.co.gridkey.api;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Collections;
import uk.co.gridkey.db.*;

public class DescribeKeyspacesDecoder {

	public Object onCall(Map<Object, Object> cassConnection) throws Exception {

		// Get the list of keyspaces from the database
		CassandraConnector cassConn =  (CassandraConnector) cassConnection.get("cassConn");
		
		List<String> cassandraKeyspaces = cassConn.getKeyspaces();
		Collections.sort(cassandraKeyspaces);

		// Build the JSON string
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();
		ArrayNode dnoArray = ((ObjectNode) rootNode).arrayNode();

		// Create a json string containing each of the DNO names
		for (String cassandraKeyspace : cassandraKeyspaces) {
			// If the keyspace is all upper case, it's one of ours and not a system keyspace
			// Replace all special characters before running this check or certain strings
			// will fail
			// Also get rid of any numbers as they aren't an issue
			// The main thing we're looking for is that the DNO ID doesn't contain lower
			// case characters (as that's reserved for system keyspaces)
			if (StringUtils
					.isAllUpperCase(cassandraKeyspace.replaceAll("[^\\p{L}\\p{Nd}]+", "").replaceAll("[0-9]", ""))) {
				// Add the Keyspace name to the list
				dnoArray.add(cassandraKeyspace);
			}
		}

		// Add the number of Entries found within the Cassandra DB to the JSON object
		((ObjectNode) rootNode).put("count", Integer.toString(dnoArray.size()));

		// Add all of the DNOs to the JSON object
		((ObjectNode) rootNode).putArray("dnos").addAll(dnoArray);

		// Convert the JSON to a string before returning it back to the flow
		return rootNode.toString();
	}
}