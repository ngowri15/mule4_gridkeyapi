package uk.co.gridkey.api;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class ConstructDetailedDataTransactionRequest {
	
	public Object onCall(Map<String, String> uriAttributes, Map<String, String> validateDetailedDataRequest) throws Exception {
		
		// Extract the required properties
		String dayNumber = uriAttributes.get("day");
		String timeOfDay = uriAttributes.get("time");
		String duration = uriAttributes.get("duration");
		String dno = uriAttributes.get("dno");
		String mcu = uriAttributes.get("mcu");
		String unlockCode = validateDetailedDataRequest.get("unlockCode");

		// Define variables
		String transactionString = null;

		DateTime timeRequestMade = DateTime.now(DateTimeZone.UTC);
		String dateTimeString = timeRequestMade.toString("yyMMddHHmmss");
		String fileNamePath = String.format("%s/%s/in/detailed_%s.bin", dno, mcu, dateTimeString);

		// See if the folder already exists. If it doesn't, create it and modify the
		// owner and permissions
		// so that the MCU can upload files
		String ftpRoot = "/data/ftp";
		Path filePath = Paths.get(ftpRoot, dno, mcu, "in");

		System.out.println("###############DNO = " + dno);
		System.out.println("###############SN = " + mcu);

		if (Files.notExists(filePath)) {
			try {
				// Create the directory and sub-directories if they do not exist (doesn't matter
				// if they do)
				Files.createDirectories(filePath);

				// Update owner - assumes that Mule is executing with root permissions
				String shellCommand = String.format("chown -R mcuftp:mcuftp %s/%s/%s/", ftpRoot, dno, mcu);
				Runtime.getRuntime().exec(shellCommand);

				System.out.println("Executed shell command");
			} catch (Exception e) {
				System.out.println("Unable to create directory and change ownership: " + filePath.toString());
			}
		}

		transactionString = String.format("%s 21 %s %s %s /%s", unlockCode, dayNumber, timeOfDay, duration,
				fileNamePath);

		System.out.println("Transaction string = " + transactionString);

		// Return the transaction string to the flow

		// Return the message back to the flow
		return transactionString;
	}
}
