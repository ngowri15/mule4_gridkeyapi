//*****************************************************************************
// Name            : Alert Email Delete Address
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
// Title                     : AlertEmailDeleteAddresses.java
//
// Author                    : J. Griffiths
//
// Related Documents         : AP50077356 New Data Centre Database SDP
//
//*****************************************************************************
// Description
// -----------
// This file deletes the email address for the given username.
//
//*****************************************************************************

package uk.co.gridkey.api;

import java.util.HashMap;
import java.util.Map;

import uk.co.gridkey.db.CassandraConnector;
import uk.co.gridkey.db.CassandraSchemaMCU520;

public class AlertEmailDeleteAddress {

	public Object onCall(Map<String, String> uriAttributes, Map<Object, Object> cassConnection) throws Exception {

		Map<Object, Object> msg = new HashMap<Object, Object>();

		String response;
		int responseCode;

		// Get the flow message
		// Extract the required properties
		String dno = uriAttributes.get("dno");
		String username = uriAttributes.get("username");

		// Get the Cassandra connector and create a new schema
		CassandraConnector cassConn =  (CassandraConnector) cassConnection.get("cassConn");
		CassandraSchemaMCU520 schema = new CassandraSchemaMCU520(cassConn);

		// The email address is valid so store it
		if (schema.removeAlertEmailAddress(dno, username)) {
			// Store was successful
			response = "{\"message\": \"success\"}";
			responseCode = 201;
		} else {
			// Store failed for some reason
			response = "{\"message\": \"error: unknown error occured whilst deleting the email address\"}";
			responseCode = 500;
		}

		msg.put("response", response);
		msg.put("responseCode", responseCode);
		msg.put("payload",response);

		return msg;
	}
}
