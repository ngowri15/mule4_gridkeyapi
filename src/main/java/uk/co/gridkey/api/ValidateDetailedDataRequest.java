package uk.co.gridkey.api;

import java.util.HashMap;
import java.util.Map;

public class ValidateDetailedDataRequest {
	private boolean ValidateDayNumber(String dayNumber) {
		boolean result = false;

		try {
			// Day number should be between 0 (Sunday) and 6 (Saturday)
			if (Integer.parseInt(dayNumber) >= 0 && Integer.parseInt(dayNumber) <= 6) {
				result = true;
			}
		} catch (Exception e) {
			System.out.println("Detailed report request: Unable to parse supplied parameter 'day'");
		}

		return result;
	}

	private boolean ValidateTimeOfDay(String timeOfDay) {
		boolean result = false;

		// Time of day should be 5 characters in the form hh:mm
		if (timeOfDay.contains(":") && timeOfDay.length() == 5) {
			// Validate each of the time fields to make sure they are in a valid range
			String timeFields[] = timeOfDay.split(":");

			try {
				Integer hoursField = Integer.parseInt(timeFields[0]);
				Integer minutesField = Integer.parseInt(timeFields[1]);

				if ((hoursField < 24) && (hoursField >= 0) && (minutesField < 60) && (minutesField >= 0)) {
					result = true;
				}
			} catch (Exception e) {
				System.out.println("Detailed report request: Unable to parse supplied parameter 'time'");
			}
		}

		return result;
	}

	private boolean ValidateDuration(String duration) {
		boolean result = false;

		try {
			// The duration can be up to half an hour at a time
			if (Integer.parseInt(duration) > 0 && Integer.parseInt(duration) < 30) {
				result = true;
			}
		} catch (Exception e) {
			// Unable to parse as an integer, therefore error condition has been met.
			// Nothing more to do now that it has been caught apart from output to
			// the console.
			System.out.println("Detailed report request: Unable to parse supplied parameter 'duration'");
		}

		return result;
	}

	public Object onCall(Map<String, String> uriAttributes) throws Exception {
		// Extract the required properties
		Map<Object, Object> msg = new HashMap<Object, Object>();

		String dayNumber = uriAttributes.get("day");
		String timeOfDay = uriAttributes.get("time");
		String duration = uriAttributes.get("duration");

		// Define variables
		Boolean result = true;

		// Perform some simple checks on each of the parameters and return the result
		// along with the transaction string
		if (ValidateDayNumber(dayNumber) && ValidateTimeOfDay(timeOfDay) && ValidateDuration(duration)) {
			result = true;
		}

		// Pass the result etc to the flow
		msg.put("result", result);

		// Return the message back to the flow
		return msg;
	}

}
