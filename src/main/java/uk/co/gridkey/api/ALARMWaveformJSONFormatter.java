//*****************************************************************************
// Name            : Decoded Message Data JSON Formatter
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
// Title                     : DecodedMessageDataJSONFormatter.java
//
// Author                    : S. Brady, J. Griffiths
//
// Related Documents         : AP50077356 New Data Centre Database SDP
//
//*****************************************************************************
// Description
// -----------
// This file contains the class that is responsible for formatting any decoded
// message data into JSON. It is designed to be invoked from a Mule flow.
//
//*****************************************************************************

package uk.co.gridkey.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import uk.co.gridkey.datacentre.AbstractJSONFormatter;

public class ALARMWaveformJSONFormatter extends AbstractJSONFormatter {
	/**
	 * WaveformDataItem is a custom class used with GSON to easily take data and
	 * parse it into a JSON string to return via the API
	 */
	public class WaveformDataItem {
		String dno;
		String mcu;
		String date;

		List<WaveformSet> data;
	}

	@SuppressWarnings("unchecked")
	public Object onCall(Map<String, String> uriAttributes, Map<Object, Object> getWaveformFiles) throws Exception {
		
		List<Map<String, String>> jsonData = new ArrayList<Map<String, String>>();

		// Get the fields, which will be used to generate the JSON data
		String dno = uriAttributes.get("dno");
		String mcu = uriAttributes.get("mcu");
		String date = uriAttributes.get("date");
		List<WaveformSet> waveforms = (List<WaveformSet>) getWaveformFiles.get("listWaveformSet");

		// Remove any null values from the list
		jsonData.removeAll(Collections.singleton(null));

		// Create the DecodedDataItem class with the
		WaveformDataItem waveformDataJson = new WaveformDataItem();
		waveformDataJson.dno = dno;
		waveformDataJson.mcu = mcu;
		waveformDataJson.date = date;
		waveformDataJson.data = waveforms;

		// Create the GSON object and get a JSON string based on the WaveformDataItem
		Gson gson = new Gson();
		return gson.toJson(waveformDataJson);
	}
}