//*****************************************************************************
// Name            : Latest Message Extract Query Contents
//
// Security Classification   : UNCLASSIFIED
//
// Copyright(s)              :
//
// The copyright in this document is the property of Lucy Electric EMS. 
// The document is supplied by Lucy Electric EMS on the express understanding 
// that it is to be treated as confidential and that it may not be copied, 
// used or disclosed to others in whole or in part for any purpose except 
// as authorised in writing by Lucy Electric EMS.
//
// Unless Lucy Electric EMS has accepted a contractual obligation in respect 
// of the permitted use of the information and data contained herein such 
// information and data is provided without responsibility and Lucy Electric EMS 
// disclaims all liability arising from its use.
//
//*****************************************************************************
// Project                   : LVSMS
//
// Title                     : LatestMessageExtractQueryContents.java
//
// Author                    : S. Brady, J. Griffiths
//
// Related Documents         : AP50077356 New Data Centre Database SDP
//
//*****************************************************************************
// Description
// -----------
// This file contains the code to extract the header information from the raw 
// message bytes that have been extracted from the latest message column family.
// It is designed to be called from a Mule flow.
//
//*****************************************************************************

package uk.co.gridkey.api;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import uk.co.gridkey.datacentre.AbstractCassandraDBProcessor;
import uk.co.gridkey.db.CassandraConnector;
import uk.co.gridkey.db.CassandraSchemaGridKeyMCU;

public class LatestMessageExtractQueryContents extends AbstractCassandraDBProcessor{
	// The following offsets are used to extract sub arrays using copyOfRange. As a
	// result the end offsets are
	// 1 beyond the last byte in the field being extracted.
	protected final static int cMSG_ID_START_OFFSET = 0;
	protected final static int cMSG_ID_END_OFFSET = 8;
	protected final static int cCOMMS_ICD_ID_START_OFFSET = 16;
	protected final static int cCOMMS_ICD_ID_END_OFFSET = 26;
	protected final static int cCOMMS_ICD_REV_START_OFFSET = 26;
	protected final static int cCOMMS_ICD_REV_END_OFFSET = 32;
	protected final static String cEXPECTED_COLUMN_NAME = "RawMsg";

	public Object onCall(Map<String, String> uriAttributes, Map<Object, Object> cassConnection) throws Exception {
		
		Map<Object, Object> msg = new HashMap<Object, Object>();
		
		// From the payload, get the raw message bytes; from here, we can get the
		// information required to load the correct JSON file

		String dno = uriAttributes.get("dno");
		String mcu = uriAttributes.get("mcu");
		String messageType = uriAttributes.get("type");
		
		// Get an instance of the schema
		CassandraConnector cassConn =  (CassandraConnector) cassConnection.get("cassConn"); 
		CassandraSchemaGridKeyMCU schema = new CassandraSchemaGridKeyMCU(cassConn);
		
		// Set Valid to false so that we do not continue with the flow
		// This will be changed to true if the method is successful

		msg.put("valid", "false");
		//msg.setProperty("valid", "false", PropertyScope.INVOCATION);

		String deviceType = schema.getDeviceType(dno, mcu);

		// Change the config type to be an actual message ID based upon what it is
		// currently set to
		if (deviceType.contains("520")) {
			switch (messageType) {
			case "fact":
			case "factory":
				// Set to factory config message ID, as per ICD definition
				messageType = "E1D2C3B480050000";
				break;

			case "user":
				// Set to user config message ID, as per ICD definition
				messageType = "E1D2C3B480050100";
				break;

			case "cal":
			case "calibration":
				// Set to calibration message ID, as per ICD definition
				messageType = "E1D2C3B480050300";
				break;

			case "stat":
			case "statistical":
			case "per":
			case "periodic":
			case "decoded":
				// Set to statistical message ID, as per ICD definition
				messageType = "E1D2C3B480010000";
				break;

			default:
				// Do nothing to change the message type. This will mean that we will attempt to
				// retrieve the message ID that was supplied
				break;
			}
		} else if (deviceType.contains("318")) {
			switch (messageType) {
			case "fact":
			case "factory":
				// Set to factory config message ID, as per ICD definition
				messageType = "E1D2C3B446414354";
				break;

			case "inst":
			case "installation":
				// Set to installation config message ID, as per ICD definition
				messageType = "E1D2C3B4494E5354";
				break;

			case "cust":
			case "customer":
				// Set to customer config message ID, as per ICD definition
				messageType = "E1D2C3B443555354";
				break;

			case "cal":
			case "calibration":
				// Set to calibration message ID, as per ICD definition
				messageType = "E1D2C3B443414C49";
				break;

			case "stat":
			case "statistical":
			case "per":
			case "periodic":
			case "decoded":
				// Set to statistical message ID, as per ICD definition
				messageType = "E1D2C3B480010000";
				break;

			default:
				// Do nothing to change the message type. This will mean that we will attempt to
				// retrieve the message ID that was supplied
				break;
			}
		} else {
			// Do nothing to change the message type. This will mean that we will attempt to
			// retrieve the message ID that was supplied
		}

		// Get the raw message bytes from the database
		ByteBuffer retrievedData = schema.getLatestMessage(dno, mcu, messageType);

		if (retrievedData != null) {
			byte[] rawMsgBytes = new byte[retrievedData.remaining()];
			retrievedData.get(rawMsgBytes);

			// Only attempt to extract the fields if rawMsgBytes is not null
			if (rawMsgBytes.length >= 1) {
				// Extract the properties required to find the correct JSON file and store them
				// in flow variables
				// We can use fixed offsets here as the header is guaranteed to be the same for
				// every message
				
				msg.put("msgID", bytesToHex(Arrays.copyOfRange(rawMsgBytes, cMSG_ID_START_OFFSET, cMSG_ID_END_OFFSET)));
				msg.put("commsICDID", new String(Arrays.copyOfRange(rawMsgBytes, cCOMMS_ICD_ID_START_OFFSET, cCOMMS_ICD_ID_END_OFFSET)));
				msg.put("commsICDRev", new String(Arrays.copyOfRange(rawMsgBytes, cCOMMS_ICD_REV_START_OFFSET, cCOMMS_ICD_REV_END_OFFSET)));
				msg.put("rawMsgBytes", rawMsgBytes);
				msg.put("valid", "true");
				
			}
		}

		
		// Return the message back to the flow with the new variables
		return msg;
	}
}