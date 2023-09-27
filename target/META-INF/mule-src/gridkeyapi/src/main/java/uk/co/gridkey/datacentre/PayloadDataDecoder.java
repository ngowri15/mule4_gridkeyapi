//*****************************************************************************
// Name            : Payload Data Decoder
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
// Title                     : PayloadDataDecoder.java
//
// Author                    : S. Brady, J. Griffiths
//
// Related Documents         : AP50077356 New Data Centre Database SDP
//
//*****************************************************************************
// Description
// -----------
// This file contains the logic necessary to decode raw binary messages. 
//
//*****************************************************************************

package uk.co.gridkey.datacentre;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PayloadDataDecoder {
	protected static final int cKLV_NAME = 0;
	protected static final int cKLV_TYPE = 1;
	protected static final int cKLV_DIVISOR_OR_LENGTH = 2;
	protected static final int cKLV_NUMBER_OF_SECTIONS = 3;
	protected static final int cHEADER_LENGTH = 66;
	protected static final int cMIN_DATA_LENGTH = 3;

	/**
	 * Traverses the KLV raw binary message, extracting the various message
	 * components into a Map of each parameter name against its corresponding value.
	 * 
	 * @param dataBuff
	 *            Raw binary message
	 * @param json
	 *            Format of the binary message expressed in JSON
	 * @return A Map of decoded payload parameter against its value
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, String> traverseKLV(byte[] dataBuff, String json) throws Exception {
		// Create a new Jackson mapper object and use it to read the json string into a
		// Map.
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> decodedJson = mapper.readValue(json, Map.class);

		// Offset the header section
		int buffPos = cHEADER_LENGTH;
		Map<String, String> extractedValueMap = new HashMap<String, String>();

		int feedercnt = 0;

		// Continue while we still have data, taking into account the 2 bytes being read
		// for
		// the initial key and length fields.
		while (buffPos < (dataBuff.length - cMIN_DATA_LENGTH)) {
			int key = dataBuff[buffPos++] & 0xFF;
			int length = dataBuff[buffPos++] & 0xFF;

			// Stop processing data if there is not enough data left to process the current
			// KLV section
			if ((buffPos + length) > dataBuff.length)
				break;

			// Decode the current section.
			Map<String, Object> section = getKLVSection(decodedJson, Integer.toString(key));

			// Ensure all sections of the JSON KLV object exist. If it doesn't, skip over
			// the data bytes.
			if (section.containsKey("Title") && section.containsKey("Length") && section.containsKey("Data")) {
				// Check that the length for the key specified matches the length field in the
				// data supplied.
				if ((Integer.parseInt(section.get("Length").toString()) == length) && (length > 0)) {
					String extractedValue = null;
					String feederRef = null;
					int tempInt = 0;

					for (ArrayList<String> jsonFormat : (ArrayList<ArrayList<String>>) section.get("Data")) {
						// Build a new List Map Map here, use the data type field to calculate the
						// number of bytes
						// to read and store.
						switch (jsonFormat.get(cKLV_TYPE).charAt(0)) {
						case 'b':
						case 'B':
						case '?':
							// Extract a single byte from the buffer.
							extractedValue = String.valueOf(dataBuff[buffPos] & 0xFF);
							buffPos += 1;
							break;
						case 'h':
						case 'H':
							// Extract 2 bytes from the buffer.
							tempInt = ((dataBuff[buffPos] & 0xFF) | ((dataBuff[buffPos + 1] & 0xFF) << 8));
							extractedValue = String.valueOf(tempInt);
							buffPos += 2;
							break;
						case 'i':
						case 'I':
						case 'l':
						case 'L':
						case 'f':
							// Extract 4 bytes from the buffer.
							tempInt = ((dataBuff[buffPos] & 0xFF) | ((dataBuff[buffPos + 1] & 0xFF) << 8)
									| ((dataBuff[buffPos + 2] & 0xFF) << 16) | ((dataBuff[buffPos + 3] & 0xFF) << 24));
							extractedValue = String.valueOf(tempInt);
							buffPos += 4;
							break;
						case 'q':
						case 'Q':
						case 'd':
							// For the time being just skip the array by 8 bytes. This will eventually
							// extract 8 bytes.
							buffPos += 8;
							break;
						case 's':
							// String is a special case, use the scale/length field to determine how much
							// data to extract.
							// For the moment however just skip the number of bytes indicated by the length
							// field.
							if (jsonFormat.get(cKLV_DIVISOR_OR_LENGTH) != "") {
								int nameLength = Integer.parseInt(jsonFormat.get(cKLV_DIVISOR_OR_LENGTH));
								extractedValue = new String(dataBuff, buffPos, nameLength).trim();
								buffPos += nameLength;
							}
							break;
						case 'e':
							// Extract a single byte from the buffer for all enum in the JSON file.
							if (jsonFormat.get(cKLV_TYPE).contains("enu-")) {
								extractedValue = String.valueOf(dataBuff[buffPos] & 0xFF);
								buffPos += 1;
							}
							break;
						default:
							// Unrecognised format. Therefore we dont want to write any contents of this
							// message to the database. Therefore
							// raise an exception so that Mule will escape execution and log it for later
							// diagnostics (should that be necessary).
							throw new Exception(new String("PayloadDataDecoder.traverseKLV - Unable to process format:"
									+ jsonFormat.get(cKLV_TYPE).charAt(0)));
						}

						// Special case, need to keep track of the current feeder ID so that it can be
						// added to the
						// feeder names before they are stored in the list - once they are added there
						// is no way of
						// determining which Feeder L1 field belongs to feeder 1, 2, 3, 4, 5 etc.
						String sectionName = jsonFormat.get(cKLV_NAME);
						if (sectionName.contains("feeder")) {
							if (sectionName.contains("reference")) {
								feederRef = extractedValue;
							} else {
								// Check this isn't a feeder reference section and modify the name to make it
								// unique by
								// adding the feeder reference - assumption is that by this point a feeder
								// reference will
								// have been encountered, due to fields within keys being of fixed order. If it
								// hasn't then
								// it will ignore this part.

								if (feederRef == null || feederRef.isEmpty()) {
									feedercnt++;
									feederRef = Integer.toString(feedercnt);
								}

								if ((sectionName.contains("in-use") == false) && !feederRef.isEmpty()) {
									sectionName = sectionName.substring(0, sectionName.indexOf('-')) + '-' + feederRef
											+ '-'
											+ sectionName.substring(sectionName.indexOf('-') + 1, sectionName.length());
								}
							}
						}

						// Put the decoded value into the value map
						extractedValueMap.put(sectionName, extractedValue);
					}
				} else {
					// Length from JSON file did not match the length in the KLV header
					// Move the buffer position by the length of the message in the hope that we
					// find a valid KLV section
					buffPos += length;
				}
			} else {
				// No Title section was found, therefore our section was not valid
				// Move the buffer position by the length of the message in the hope that we
				// find a valid KLV section
				buffPos += length;
			}
		}

		return extractedValueMap;
	}

	/**
	 * Gets the KLV section specified from the JSON file provided
	 * 
	 * @param json
	 *            JSON file representing the message format
	 * @param SectionNo
	 *            Section number in the JSON file to be extracted
	 * @return The section of the JSON file requested, or an empty Map if the
	 *         section cannot be found
	 */
	private static Map<String, Object> getKLVSection(Map<String, Object> json, String SectionNo) {
		// Generate a map to contain the various KLV sections, but only populate if the
		// section number is valid
		// for the ICD number/version supplied in the json string.
		Map<String, Object> section = new HashMap<String, Object>();

		// Check that the Section exists before attempting to get data
		if (((Map<?, ?>) json.get("binary-format")).get(SectionNo) != null) {
			// Ensure there are cKLV_NUMBER_OF_SECTIONS elements in the JSON section (Title,
			// Length and Data)
			if (((List<?>) ((Map<?, ?>) json.get("binary-format")).get(SectionNo)).size() == cKLV_NUMBER_OF_SECTIONS) {
				section.put("Title", ((List<?>) ((Map<?, ?>) json.get("binary-format")).get(SectionNo)).get(cKLV_NAME));
				section.put("Length",
						((List<?>) ((Map<?, ?>) json.get("binary-format")).get(SectionNo)).get(cKLV_TYPE));
				section.put("Data",
						((List<?>) ((Map<?, ?>) json.get("binary-format")).get(SectionNo)).get(cKLV_DIVISOR_OR_LENGTH));
			}
		}
		return section;
	}
}