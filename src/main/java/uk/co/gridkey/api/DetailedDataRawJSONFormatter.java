//*****************************************************************************
// Name            : Config Data Raw JSON Formatter
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
// Title                     : ConfigDataRawJSONFormatter.java
//
// Author                    : S. Brady, J. Griffiths
//
// Related Documents         : AP50077356 New Data Centre Database SDP
//
//*****************************************************************************
// Description
// -----------
// This file contains the logic for formating raw binary data as JSON. The 
// raw binary data is converted into ascii readable form. It is specific to the
// configuration data, in particular processing the latest configs, as the 
// latest configs are stored in a different format to other raw binary 
// messages. This is designed to be invoked from a Mule flow.
//
//*****************************************************************************

package uk.co.gridkey.api;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

import uk.co.gridkey.datacentre.AbstractJSONFormatter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DetailedDataRawJSONFormatter extends AbstractJSONFormatter {
	/**
	 * Generates the JSON string representation of the latest binary config data
	 * 
	 * @param rawMsgBytes
	 *            The raw binary message data from the detailed data received from
	 *            the MCU
	 * @param dno
	 *            DNO identity
	 * @param mcu
	 *            MCU serial number
	 * @param type
	 *            Message type
	 * @param detailedDataTimestamp
	 *            The timestamp when the data was requested
	 * @return String JSON formatted representation of the raw binary data for the
	 *         requested configuration message
	 */
	private String GenerateJSON(byte[] rawMsgBytes, String dno, String mcu, String type, String msgTimestamp) {
		// Create a new ObjectMapper and set the rootNode to a new objectnode
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();

		// The fist elements in the JSON root are fixed format to help when determining
		// what the data represents
		((ObjectNode) rootNode).put("dno", dno);
		((ObjectNode) rootNode).put("mcu", mcu);
		((ObjectNode) rootNode).put("type", type);
		((ObjectNode) rootNode).put("datetime", msgTimestamp);
		((ObjectNode) rootNode).put("data", bytesToHex(rawMsgBytes));

		// Convert the rootNode to a string before returning it
		return rootNode.toString();
	}

	private Path GetLatestDetailedDataFilePath(String dno, String mcu) {
		String ftpRoot = "/data/ftp";
		Path detailedFilePath = null;

		// Get a list of .bin files in the FTP directory that are detailed data files
		Path ftpPath = Paths.get(ftpRoot, dno, mcu, "in");
		File folder = ftpPath.toFile();
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".bin") && name.toLowerCase().contains("detailed");
			}
		};

		// Check each file that matches for the latest request time (which is embedded
		// in the file name)
		File[] listOfFiles = folder.listFiles(filter);

		Arrays.sort(listOfFiles);

		if (listOfFiles.length >= 0) {
			detailedFilePath = Paths.get(ftpPath.toString(), listOfFiles[listOfFiles.length - 1].getName());
		}

		return detailedFilePath;
	}

	public Object onCall(Map<String, String> uriAttributes) throws Exception {
		// Set jsonData to an empty JSON node initially
		String jsonData = "{}";
		Path latestFileWithPath;

		// Get the parameters supplied in the URI, such as MCU serial number and dno
		String mcu = uriAttributes.get("mcu");
		String dno = uriAttributes.get("dno");

		// Get the latest file (if one exists) and format it
		latestFileWithPath = GetLatestDetailedDataFilePath(dno, mcu);
		if (latestFileWithPath != null) {
			// Read in the data from the latest file received
			byte[] rawMsgBytes = Files.readAllBytes(latestFileWithPath);

			// Extract the date time from the latest file received common header
			String msgTimestamp = ExtractMsgTimestamp(rawMsgBytes);

			// Format the data as JSON and pass back to the mule flow
			jsonData = GenerateJSON(rawMsgBytes, dno, mcu, "detailed", msgTimestamp);
		}

		return jsonData;
	}
}
