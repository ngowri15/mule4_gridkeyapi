/*
 * =============================================================================
 *
 * Classification: COMMERCIAL IN CONFIDENCE
 *
 * Project:        GridKey
 * Title:          Grid Time to Date Time class
 * Author(s):      Jordon Griffiths
 *
 * Copyright Notice
 * ----------------
 *
 * The information contained in this document is proprietary to Selex ES unless
 * stated otherwise and is made available in confidence; it must not be used or
 * disclosed without the express written permission Selex ES. This document may
 * not be copied in whole or in part in any form without the express written
 * consent of Selex ES.
 *
 * Public Access: Freedom Of Information Act 2000
 *
 * This document contains trade secrets and/or sensitive commercial information
 * as of the date provided to the original recipient by Selex ES and is
 * provided in confidence. Following a request for this information public
 * authorities are to consult with Selex ES regarding the current releasability
 * of the information prior to the decision to release all or part of this
 * document.Release of this information by a public authority may constitute an
 * actionable breach of confidence.
 *
 * UK Origin
 *
 * Selex ES Limited
 * Christopher Martin Road Basildon Essex SS14 3EL England
 * Telephone: 01268 522822 Facsimile: 01268 883140
 *
 * =============================================================================
 * Description
 * -----------------------------------------------------------------------------
 *
 * This class provides helper functions to convert the timestamp stored in the
 * Data Centre database into a string representing the "printable" date and time
 * or the KLV timestamp.
 *
 * =============================================================================
 */

package uk.co.gridkey.api;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

public class TimeConverter {
	public static String dateTimeFormatPattern = "yyMMddHHmmss";

	public static String GridTimeToDateTimeForCsv(String gridTime) {
		return gridTime.substring(4, 6) + "/" + gridTime.substring(2, 4) + "/20" + gridTime.substring(0, 2) + " "
				+ gridTime.substring(6, 8) + ":" + gridTime.substring(8, 10) + ":" + gridTime.substring(10, 12);
	}

	public static String GridTimeToDateTimeForFilename(String gridTime) {
		return "20" + gridTime.substring(0, 2) + "-" + gridTime.substring(2, 4) + "-" + gridTime.substring(4, 6);
	}

	public static DateTime GridTimeToJodaTime(String gridTime) {
		return DateTime.parse(gridTime, DateTimeFormat.forPattern(dateTimeFormatPattern));
	}

	public static Long GridTimeToUnixTime(String gridTime) {
		return DateTime.parse(gridTime, DateTimeFormat.forPattern(dateTimeFormatPattern)).getMillis() / 1000L;
	}

	public static Long JodaTimeToUnixTime(DateTime gridTime) {
		return gridTime.getMillis() / 1000L;
	}
}

/*
 * =============================================================================
 * Classification: COMMERCIAL IN CONFIDENCE
 * =============================================================================
 */
