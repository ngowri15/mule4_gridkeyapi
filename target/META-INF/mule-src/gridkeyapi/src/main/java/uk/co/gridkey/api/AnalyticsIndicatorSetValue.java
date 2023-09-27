//*****************************************************************************
// Name            : Analytics Indicator Set Value
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
// Title                     : AnalyticsIndicatorSetValue.java
//
// Author                    : J. Griffiths
//
// Related Documents         : AP50077356 New Data Centre Database SDP
//
//*****************************************************************************
// Description
// -----------
// Sets the value of the analytics indicator for the given MCU.
//
//*****************************************************************************

package uk.co.gridkey.api;

import java.util.HashMap;
import java.util.Map;

import uk.co.gridkey.db.CassandraConnector;
import uk.co.gridkey.db.CassandraSchemaMCU520;

public class AnalyticsIndicatorSetValue {

	public Object onCall(Map<String, String> uriAttributes, String indicatorVal, Map<Object, Object> cassConnection) throws Exception {

		Map<Object, Object> msg = new HashMap<Object, Object>();
		
		// Get the required parameters from the flow
		String mcu = uriAttributes.get("mcu").toString();
		String dno = uriAttributes.get("dno").toString();
		String indicator = uriAttributes.get("indicator");
		String indicatorValue = indicatorVal.toString().toLowerCase();

		// Get the Cassandra connector and create a new schema
		CassandraConnector cassConn =  (CassandraConnector) cassConnection.get("cassConn");
		CassandraSchemaMCU520 schema = new CassandraSchemaMCU520(cassConn);

		// Assume that the store was successful - we'll change this below if something
		// is wrong
		String response = "{\"message\": \"success\"}";
		int responseCode = 201;

		switch (indicator.toLowerCase()) {
		case "faults":
		case "fault":
		case "f":
			schema.updateAnalyticsFaultStatus(dno, mcu, indicatorValue);
			break;
		case "losses":
		case "loss":
		case "l":
			schema.updateAnalyticsLossesStatus(dno, mcu, indicatorValue);
			break;
		case "planning":
		case "plan":
		case "p":
			schema.updateAnalyticsPlanningStatus(dno, mcu, indicatorValue);
			break;
		default:
			// Do nothing here, we can't set something that we don't know...
			response = "{\"message\": \"Error: unknown indicator type. Please use faults, losses or planning (or F L P for short).\"}";
			responseCode = 500;
			break;
		}

		msg.put("response", response);
		msg.put("responseCode", responseCode);
		msg.put("paylaod",response);

		return msg;
	}

}
