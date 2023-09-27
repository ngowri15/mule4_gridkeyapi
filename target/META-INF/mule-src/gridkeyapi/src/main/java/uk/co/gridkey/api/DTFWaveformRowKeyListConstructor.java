//*****************************************************************************
// Name            : Decoded Message Data Row Key List Constructor
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
// Title                     : DecodedMessageDataRowKeyListConstructor.java
//
// Author                    : S. Brady, J. Griffiths
//
// Related Documents         : AP50077356 New Data Centre Database SDP
//
//*****************************************************************************
// Description
// -----------
// This file contains the class for generating the row key list used to extract
// the decoded message data stored within the Cassandra database. It has been
// designed to be invoked from a Mule flow.
//
//*****************************************************************************

package uk.co.gridkey.api;

import java.util.HashMap;
import java.util.Map;

public class DTFWaveformRowKeyListConstructor {
	
	public Object onCall(Map<String, String> uriAttributes) throws Exception {
		// Get the various session variables that are required to populate the other
		// variables required
		Map<Object, Object> msg = new HashMap<Object, Object>();

		String date = uriAttributes.get("date");
		String params = new String(
				"V1 DTF Waveform,V2 DTF Waveform,V3 DTF Waveform,I1 DTF Waveform,I2 DTF Waveform,I3 DTF Waveform");

		// Generate the row keys and pass them back to the mule flow
		msg.put("params", params);
		msg.put("start", date);
		msg.put("end", date);

		return msg;
	}
}
