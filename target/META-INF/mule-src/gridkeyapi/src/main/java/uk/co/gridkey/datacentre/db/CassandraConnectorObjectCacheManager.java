//*****************************************************************************
// Name            : Cassandra Connector Object Cache Manager
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

package uk.co.gridkey.datacentre.db;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import uk.co.gridkey.db.*;

public class CassandraConnectorObjectCacheManager {

	private Ehcache cache; 
	private final String cassConnectorKey = "cassConn";
	private final String username = "groot";
	private final String password = "W3n3@r7yw0rk3d0n@f@rmw1th2p1gmyg0@tsc@773dB177&B3n&a5h1r3h0rs3ca77edR0cky";

	/**
	 * Required function, auto generated to set the cache instance from the bean
	 * definition in the flow XML
	 * 
	 * @param cache Cache being used by this manager. Invoked by the bean in the
	 *              flow
	 */
	/*
	public void setCache(final Ehcache cache) {
		this.cache = cache;
	}
	*/

	public Object onCall() {
		
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

		Map<Object, Object> msg = new HashMap<>();
		try {
			boolean connectionOK = false;

			// Determine if the connector already exists within the cache i.e. this isnt the
			// first
			// message to be processed
			
			final Element cachedElement = cache.get(cassConnectorKey);

			if (cachedElement == null) {
				CassandraConnector cass = null;

				// Read in the IP address to be used - or just use the default if no file can be
				// found
				String seedNode = readSeedNode();

				// Create a new connection and connect
				if (seedNode != null) {
					cass = new CassandraConnector(seedNode);
				} else {
					// Use the default instead
					cass = new CassandraConnector();
				}

				connectionOK = cass.open(username, password);

				if (cass != null && cass.getSession() != null) {
					cache.put(new Element(cassConnectorKey, cass));
				}
			}

			// Read out the Cassandra connector stored in cache and return it to the Mule
			// flow
			Element tempElement = cache.get(cassConnectorKey);
			if ((tempElement != null) && (tempElement.getObjectValue() != null)) {
				CassandraConnector cass = (CassandraConnector) cache.get(cassConnectorKey).getObjectValue();

				// Check that between calls the session hasn't closed. If it has reconnect
				if (cass != null && cass.getSession().isClosed()) {
					connectionOK = cass.open(username, password);

					// On successful reconnection put back into cache
					if (cass != null && cass.getSession() != null) {
						cache.put(new Element(cassConnectorKey, cass));
					}
				} else {
					// Session OK (not null) and not closed
					connectionOK = true;
				}
				// msg.setProperty("cassConn", cass, PropertyScope.INVOCATION);
				msg.put("cassConn", cass);
			}

			// msg.setProperty("connectionOK", connectionOK ? "TRUE" : "FALSE",
			// PropertyScope.SESSION);
			msg.put("connectionOK", connectionOK ? "TRUE" : "FALSE");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return msg;
	}

	public String readSeedNode() {
		String seedNode = null;

		// Detect if Windows or Linux as the files will be stored separately from the
		// mule deployment
		// and in different locations depending upon the operating system.
		String seedFileAndPath = null;

		if (System.getProperty("os.name").startsWith("Windows")) {
			// Assume Windows
			seedFileAndPath = "D:/data/dbseed/seednode.cfg";
		} else {
			// Assume Linux
			seedFileAndPath = "/Users/alphanove/Downloads/Lucy_Electric/Mule4_Migration/gridkeyapi/seednode.cfg";
		}

		// Ensure the JSON file exists
		if (Files.exists(Paths.get(seedFileAndPath))) {
			try {
				// Expectation is for there to be a single IP address in the file, nothing else
				// e.g. 10.222.2.11
				// and it be written in plain text format
				seedNode = new String(Files.readAllBytes(Paths.get(seedFileAndPath)), "UTF-8");

				System.out.println("Cassandra seed node file located, using: " + seedNode);
			} catch (UnsupportedEncodingException e) {
				// The exception will be logged to the default Mule exception log to assist in
				// future diagnostics
				e.printStackTrace();
			} catch (IOException e) {
				// The exception will be logged to the default Mule exception log to assist in
				// future diagnostics
				e.printStackTrace();
			}
		} else {
			System.out.println("#ERROR# Seednode file could not be located at: " + seedFileAndPath);
		}

		return seedNode;
	}
}
