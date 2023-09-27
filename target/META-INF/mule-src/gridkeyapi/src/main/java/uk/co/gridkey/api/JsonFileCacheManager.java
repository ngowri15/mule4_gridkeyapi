//*****************************************************************************
// Name            : Json File Cache Manager
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
// Title                     : JsonFileCacheManager.java
//
// Author                    : S. Brady, J. Griffiths
//
// Related Documents         : AP50077356 New Data Centre Database SDP
//
//*****************************************************************************
// Description
// -----------
// This file contains the necessary logic to avoid breaking the first rule of 
// Mule club...don't use Enterprise connectors. It is a manager for the ehcache
// open source in memory caching. It is used to manage access to the JSON file
// format specifications to avoid excessive disk I/O. It is designed to be 
// invoked from a Mule flow.
//
//*****************************************************************************

package uk.co.gridkey.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

public class JsonFileCacheManager {
	private Ehcache cache;

	/**
	 * Required function, auto generated to set the cache instance from the bean
	 * definition in the flow XML
	 * 
	 * @param cache
	 *            Cache being used by this manager.
	 */

	public Object onCall(Map<String, String> latestMessageExtractQueryContents) throws Exception {
		
		if (cache == null) {
            // Create the cache manager (if not already created)
            CacheManager cacheManager = CacheManager.getInstance();

            // Check if the cache already exists in the cache manager
            if (!cacheManager.cacheExists("gridkeyCache")) {
                // If it doesn't exist, create a new cache
                cacheManager.addCache(new Cache("gridkeyCache", 10000, false, false, 600000, 600000));
            }

            // Get the reference to the cache
            cache = cacheManager.getEhcache("gridkeyCache");
        }
		
		
		// Build the name of the JSON file that needs to be extracted, pulling in the
		// various fields
		// extracted in a previous Mule flow.

		// Detect if Windows or Linux as the files will be stored separately from the
		// mule deployment
		// and in different locations depending upon the operating system.
		String jsonFileAndPath = null;
		String jsonFileContent = null;
		String msgID = latestMessageExtractQueryContents.get("msgID");
		String commsICDID = latestMessageExtractQueryContents.get("commsICDID");
		String commsICDRev = latestMessageExtractQueryContents.get("commsICDRev");
		
		if (System.getProperty("os.name").startsWith("Windows")) {
			// Assume Windows
			jsonFileAndPath = String.format("D:/Development/Projects/Gridkey/CassandraFormats/%1$s/%2$s/%3$s.json",
					commsICDID, commsICDRev, msgID);
		} else {
			// Assume Linux
			jsonFileAndPath = String.format("/data/cassandraformats/%1$s/%2$s/%3$s.json", commsICDID, commsICDRev,
					msgID);
		}

		// Ensure the JSON file exists
		if (Files.exists(Paths.get(jsonFileAndPath))) {
			// Determine if that file already exists within the cache i.e. has JSON file
			// been loaded before?
			final Element cachedElement = cache.get(jsonFileAndPath);
			if (cachedElement == null) {
				// Element not in cache, so read in the file and add it to the cache
				String json = null;

				try {
					json = new String(Files.readAllBytes(Paths.get(jsonFileAndPath)), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// The exception will be logged to the default Mule exception log to assist in
					// future diagnostics
					e.printStackTrace();
				} catch (IOException e) {
					// The exception will be logged to the default Mule exception log to assist in
					// future diagnostics
					e.printStackTrace();
				}

				cache.put(new Element(jsonFileAndPath, json));
			}

			// Read out the JSON file stored in cache and return it to the Mule flow
			Element tempElement = cache.get(jsonFileAndPath);
			if (tempElement.getObjectValue() != null) {
				String temp = cache.get(jsonFileAndPath).getObjectValue().toString();
				//msg.setProperty("jsonFileContent", temp, PropertyScope.SESSION);
				jsonFileContent = temp; 
			}
		} else {
			System.out.print("#ERROR# JSON ICD is not located at: " + jsonFileAndPath);
		}

		return jsonFileContent;
	}
}