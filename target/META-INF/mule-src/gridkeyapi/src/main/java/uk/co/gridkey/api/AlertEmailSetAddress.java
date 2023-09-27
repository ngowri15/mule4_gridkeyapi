//*****************************************************************************
// Name            : Alert Email Set Addresses
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
// Title                     : AlertEmailSetAddresses.java
//
// Author                    : J. Griffiths
//
// Related Documents         : AP50077356 New Data Centre Database SDP
//
//*****************************************************************************
// Description
// -----------
// This file updates the list of the email addresses associated with the given
// DNO which are enabled to receive alert emails.
//
//*****************************************************************************

package uk.co.gridkey.api;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.validator.routines.EmailValidator;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.co.gridkey.db.CassandraConnector;
import uk.co.gridkey.db.CassandraSchemaMCU520;

public class AlertEmailSetAddress {

	public Object onCall(Map<String, String> uriAttributes, Map<Object, Object> cassConnection, String payload) throws Exception {
		
		Map<Object, Object> msg = new HashMap<Object, Object>();
		
		String response;
		int responseCode;

		// Create an email validator
		EmailValidator validator = EmailValidator.getInstance();

		// Get the flow message

		// Extract the required properties
		String dno = uriAttributes.get("dno");
		String username = uriAttributes.get("username");

		// Create a new Jackson mapper object and use it to read the json string into a
		// Map.
		ObjectMapper mapper = new ObjectMapper();
		@SuppressWarnings("unchecked")
		Map<String, Object> decodedJson = mapper.readValue(payload, Map.class);
		String emailAddress = (String) decodedJson.get("emailaddress");

		// Get the Cassandra connector and create a new schema
		CassandraConnector cassConn =  (CassandraConnector) cassConnection.get("cassConn");
		CassandraSchemaMCU520 schema = new CassandraSchemaMCU520(cassConn);

		// Validate the email address
		if (validator.isValid(emailAddress)) {
			// The email address is valid so store it
			if (schema.addAlertEmailAddress(dno, username, emailAddress)) {
				// Store was successful
				response = "{\"message\": \"success\"}";
				responseCode = 201;
			} else {
				// Store failed for some reason
				response = "{\"message\": \"error: unknown error occured whilst storing the email address\"}";
				responseCode = 500;
			}
		} else {
			// The email address is invalid
			response = "{\"message\": \"error: invalid email address supplied: " + emailAddress + "\"}";
			responseCode = 400;
		}

		msg.put("response", response);
		msg.put("responseCode", responseCode);
		msg.put("payload",response);

		return msg;
	}
}
