package uk.co.gridkey.token;

import java.io.IOException;
import java.io.InputStream;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Properties;

public class SecurityConstants {

	
	public static final String getSecret() throws IOException {

		String secretKey = null;

		try (InputStream input = SecurityConstants.class.getClassLoader().getResourceAsStream("secrets.properties")) {

			Properties properties = new Properties();
			properties.load(input);
			secretKey = properties.getProperty("JWT_SECRET");

		} catch (Exception e) {
			System.out.println(e);
		}

		return secretKey;
	}
	

	public static final long getJWTAccessTokenExpiration(int months) {

		
		// Get the current date
		LocalDateTime currentDateTime = LocalDateTime.now();

		// Add the specified number of months to the current date
		LocalDateTime futureDateTime = currentDateTime.plusMonths(months);

		// Calculate the duration between the current date and the future date in
		// milliseconds
		long milliseconds = ChronoUnit.MILLIS.between(currentDateTime, futureDateTime);

		return milliseconds;
	}
}
