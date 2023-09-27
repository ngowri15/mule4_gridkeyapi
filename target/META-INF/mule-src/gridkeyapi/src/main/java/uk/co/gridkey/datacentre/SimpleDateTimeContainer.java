//*****************************************************************************
// Name            : Simple Date Time Container
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
// Title                     : SimpleDateTimeContainer.java
//
// Author                    : S. Brady, J. Griffiths
//
// Related Documents         : AP50077356 New Data Centre Database SDP
//
//*****************************************************************************
// Description
// -----------
// This file contains the simple date/time container for storing timestamp
// information and instance values that are passed around during the 
// data extraction and JSON formatting methods
//
//*****************************************************************************

package uk.co.gridkey.datacentre;

public class SimpleDateTimeContainer {
	private String dateTime;
	private String instanceNumber;

	/**
	 * Default constructor
	 */
	public SimpleDateTimeContainer() {
		dateTime = null;
		instanceNumber = null;
	}

	/**
	 * Sets the dateTime field in this class
	 * 
	 * @param dateTime
	 */
	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}

	/**
	 * Gets the dateTime field in this class
	 * 
	 * @return The dateTime String in the form YYMMDDHHMMSS
	 */
	public String getDateTime() {
		return dateTime;
	}

	/**
	 * Sets the instance number field in this class
	 * 
	 * @param instanceNumber
	 */
	public void setInstanceNumber(String instanceNumber) {
		this.instanceNumber = instanceNumber;
	}

	/**
	 * Gets the instance number field in this class
	 * 
	 * @return The instanceNumber field. This may be null
	 */
	public String getInstanceNumber() {
		return instanceNumber;
	}
}
