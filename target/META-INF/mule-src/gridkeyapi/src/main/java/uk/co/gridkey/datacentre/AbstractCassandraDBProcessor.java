//*****************************************************************************
// Name            : Abstract Cassandra DB Processor
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
// Title                     : AbstractCassandraDBProcessor.java
//
// Author                    : S. Brady, J. Griffiths
//
// Related Documents         : AP50077356 New Data Centre Database SDP
//
//*****************************************************************************
// Description
// -----------
// This file contains the abstract class for performing Cassandra database
// related operations. Common functions are provided that are to be used across
// multiple inherited sub classes. 
//
//*****************************************************************************

package uk.co.gridkey.datacentre;

public abstract class AbstractCassandraDBProcessor {
	final protected static char[] hexCharacterArray = "0123456789ABCDEF".toCharArray();

	/**
	 * Helper function to decode a byte array into the hex string equivalent
	 * 
	 * @param dataBytes
	 *            Data to be converted
	 * @return A String representation of the message bytes
	 */
	protected String bytesToHex(byte[] dataBytes) {
		// The hex string will be twice as long as the original byte representation, so
		// allocate a sufficiently sized buffer
		char[] hexCharRepresentation = new char[dataBytes.length * 2];

		// For each byte, look up the corresponding element in the hex character array,
		// operating nibble at a time
		for (int index = 0; index < dataBytes.length; index++) {
			int value = dataBytes[index] & 0xFF;
			hexCharRepresentation[index * 2] = hexCharacterArray[value >>> 4];
			hexCharRepresentation[index * 2 + 1] = hexCharacterArray[value & 0x0F];
		}

		// Convert character array to a string
		return new String(hexCharRepresentation);
	}
}
