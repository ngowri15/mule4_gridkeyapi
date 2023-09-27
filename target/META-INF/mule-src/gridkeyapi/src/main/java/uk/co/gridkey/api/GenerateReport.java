package uk.co.gridkey.api;

import java.util.Base64;
import java.util.Map;

public class GenerateReport {
	Process mProcess;

	public Object onCall(Map<String, String> uriAttributes) throws Exception {
		// TODO Auto-generated method stub

		// From the payload, get the raw message bytes; from here, we can get the
		// information required to return the correct JSON file
		
		String basePath = "";
		
		String dno = uriAttributes.get("dno");
		String mcu = uriAttributes.get("mcu");
		String startDate = uriAttributes.get("start");
		String endDate = uriAttributes.get("end");
		String param = uriAttributes.get("param");

		if (System.getProperty("os.name").startsWith("Windows")) {
			// Assume Windows
			if (param.equals("autoloopconnected"))
				basePath = "C:/Lucy/Processing/reports/src/report.bat";
			else if (param.equals("onlymcu"))
				basePath = "C:/Lucy/Processing/reports/src/onlymcu.bat";
			else
				basePath = "C:/Lucy/Processing/reports/src/report.bat";
		} else {
			// Assume Linux
			if (param.equals("autoloopconnected"))
				basePath = "/data/reports/scripts/report.sh";
			else if (param.equals("onlymcu"))
				basePath = "/data/reports/scripts/onlymcu.sh";
			else
				basePath = "/data/reports/scripts/report.sh";
		}

		try {

			basePath = basePath + " " + mcu + " " + dno + " " + startDate + " " + endDate;
			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.command("bash", "-c", basePath);
			processBuilder.start();
			Thread.sleep(5000);

		} catch (Exception e) {
			return "failed";
		}

		return "success";
	}

	public String decrypt(String originalInput) {
		byte[] decodedBytes = Base64.getDecoder().decode(originalInput);
		return new String(decodedBytes);
	}

}
