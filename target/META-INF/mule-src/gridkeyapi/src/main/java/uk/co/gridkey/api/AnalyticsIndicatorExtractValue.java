//*****************************************************************************
// Name            : Analytics Indicator Extract Value
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
// Title                     : AnalyticsIndicatorExtractValue.java
//
// Author                    : J. Griffiths
//
// Related Documents         : AP50077356 New Data Centre Database SDP
//
//*****************************************************************************
// Description
// -----------
// Extracts the indicator value from the given JSON string (assumes that the
// string is on the payload).
//
//*****************************************************************************

package uk.co.gridkey.api;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AnalyticsIndicatorExtractValue {

	@SuppressWarnings("unchecked")
	public Object onCall(String payload) throws Exception {
		
		Map<Object, Object> msg = new HashMap<Object, Object>();
		
		// Create a new Jackson mapper object and use it to read the json string into a
		// Map
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> decodedJson = mapper.readValue(payload, Map.class);
		String indicatorValue = (String) decodedJson.get("indicatorValue");

		msg.put("indicatorValue", indicatorValue);

		return msg;
	}

}
