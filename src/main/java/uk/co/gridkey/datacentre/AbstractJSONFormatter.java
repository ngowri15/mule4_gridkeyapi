//*****************************************************************************
// Name            : Abstract JSON Formatter
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
// Title                     : AbstractJSONFormatter.java
//
// Author                    : S. Brady, J. Griffiths
//
// Related Documents         : AP50077356 New Data Centre Database SDP
//
//*****************************************************************************
// Description
// -----------
// This file contains the abstract class that contains common functions for 
// formatting JSON.
//
//*****************************************************************************

package uk.co.gridkey.datacentre;

import java.util.Arrays;
import org.joda.time.DateTime;

public abstract class AbstractJSONFormatter {
	/**
	 * JSON strings return the timestamp information of the objects in the JSON root
	 * node. Use the raw message bytes to extract this information as a formatted
	 * string so that it can be used for this purpose.
	 * 
	 * @param rawMsgBytes
	 *            Raw binary message bytes.
	 * @return String containing the timestamp in the YYMMDDHHMMSS format.
	 */
	protected String ExtractMsgTimestamp(byte[] rawMsgBytes) {
		// The timestamp is in the common header. Therefore it is safe to assume it is
		// in a fixed location. Just read the
		// 12 bytes that represent the field in YYMMDDHHMMSS format.
		byte[] timestamp = Arrays.copyOfRange(rawMsgBytes, 54, 66);
		return new String(timestamp);
	}

	final protected static char[] hexCharacterArray = "0123456789ABCDEF".toCharArray();

	/**
	 * Helper function to decode a byte array into the hex string equivalent.
	 * 
	 * @param dataBytes
	 *            Data to be converted
	 * @return Hex String representation of the raw binary data bytes supplied
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

	/**
	 * Extracts the field/parameter name from a row key string. Assumes that the row
	 * key is in the form SN:MM-DD:NAME
	 * 
	 * @param rowKey
	 *            Row key containing field to be extracted
	 * @return String containing the row key field name
	 */
	protected String ExtractFieldNameFromRowKey(String rowKey) {
		String fieldName = null;

		try {
			// Row key is in the form SN:MM-DD:PARAM therefore extract PARAM from the string
			fieldName = rowKey.substring(rowKey.lastIndexOf(':') + 1, rowKey.length());
		} catch (Exception e) {
			// Nothing can be done, format is incorrect, therefore return null
			return null;
		}

		return fieldName;
	}

	/**
	 * Generates the date and time string based upon information from various
	 * different places
	 * 
	 * @param rowKey
	 *            Row key used to extract the day and month information
	 * @param secondsSinceMidnight
	 *            To generate the hours, minutes and seconds in the time string
	 * @param referenceDateTimeString
	 *            To extract the year information for the time string
	 * @return Date time string in the format YYMMDDhhmmss
	 */
	protected String GenerateDateTimeString(String rowKey, String secondsSinceMidnight,
			String referenceDateTimeString) {
		String day = null;
		String month = null;
		String year = null;

		try {
			// Row key is in the form SN:MM-DD:PARAM therefore extract the month and day. As
			// explicitly accessing
			// elements in the substring it could throw an exception if there aren't enough
			// characters, or if the
			// string isn't in the correct format.
			day = rowKey.substring(rowKey.indexOf('-') + 1, rowKey.indexOf('-') + 3);
			month = rowKey.substring(rowKey.indexOf(':') + 1, rowKey.indexOf(':') + 3);

			// Take the year from referenceDataTimeString, which is in the form
			// YYMMDDhhmmss. We are only interested
			// in the 2 year digits at the beginning.
			year = referenceDateTimeString.substring(0, 2);
		} catch (Exception e) {
			// Nothing can be done, format is incorrect, therefore return null
			return null;
		}

		// Get hours from seconds since midnight by dividing by 60 seconds x 60 minutes
		// per hour
		int hours = Integer.parseInt(secondsSinceMidnight) / 3600;
		// Get minutes by subtracting the seconds worth of hours from above, then
		// dividing the remaining seconds by
		// 60 to get minutes
		int minutes = (Integer.parseInt(secondsSinceMidnight) - (hours * 3600)) / 60;
		// Get seconds by subtracting the seconds worth of hours and minutes. The
		// remainder is the seconds
		int seconds = Integer.parseInt(secondsSinceMidnight) - (hours * 3600) - (minutes * 60);

		// Format the date and time as YYMMDDhhmmss
		String dateTimeString = String.format("%s%s%s%02d%02d%02d", year, month, day, hours, minutes, seconds);
		return dateTimeString;
	}

	/**
	 * Generates the date time string based upon information from various different
	 * places. This version takes a Date and extracts the Day, Month and Year from
	 * there
	 * 
	 * @param timestamp
	 *            A JodaTime DateTime value which represents the date of the report
	 * @param secondsSinceMidnight
	 *            To generate the hours, minutes and seconds in the time string
	 * @return
	 */
	protected SimpleDateTimeContainer GenerateDateTimeString(DateTime timestamp, String secondsSinceMidnight) {
		String generatedDateTime;
		String instanceNumber;

		// Get the day and month values from the timestamp provided
		String day = String.format("%02d", timestamp.getDayOfMonth());
		String month = String.format("%02d", timestamp.getMonthOfYear());

		// Take the year from the timestamp; we are only interested in the 2 year digits
		// at the beginning.
		String year = String.format("%02d", timestamp.getYearOfCentury());

		// Before decoding the seconds since midnight field, need to determine if there
		// are any
		// instance indicators present i.e. multiple alerts can be generated per second
		// and the
		// resolution of the Data Centre is 1 second. Therefore in these instances, data
		// is
		// stored <seconds since midnight>:<instance number>
		if (secondsSinceMidnight.contains(":")) {
			String[] secondsSinceMidnightFields = secondsSinceMidnight.split(":");
			secondsSinceMidnight = secondsSinceMidnightFields[0];
			instanceNumber = new String(secondsSinceMidnightFields[1]);
		} else {
			// Just set the instance number to null as it isnt valid
			instanceNumber = null;
		}

		// Get hours from seconds since midnight by dividing by 60 seconds x 60 minutes
		// per hour
		int hours = Integer.parseInt(secondsSinceMidnight) / 3600;
		// Get minutes by subtracting the seconds worth of hours from above, then
		// dividing the remaining seconds by
		// 60 to get minutes
		int minutes = (Integer.parseInt(secondsSinceMidnight) - (hours * 3600)) / 60;
		// Get seconds by subtracting the seconds worth of hours and minutes. The
		// remainder is the seconds
		int seconds = Integer.parseInt(secondsSinceMidnight) - (hours * 3600) - (minutes * 60);

		// Format the date and time as YYMMDDhhmmss and return both fields
		generatedDateTime = new String(String.format("%s%s%s%02d%02d%02d", year, month, day, hours, minutes, seconds));

		SimpleDateTimeContainer dateTime = new SimpleDateTimeContainer();
		dateTime.setDateTime(generatedDateTime);
		dateTime.setInstanceNumber(instanceNumber);
		return dateTime;
	}
}
