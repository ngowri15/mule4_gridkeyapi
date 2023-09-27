package uk.co.gridkey.token;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GenerateAccessToken  {
	
	protected static final Logger logger = LogManager.getLogger();
	
	private static JWTImplementation jwtImplementation = new JWTImplementation();

	public static String generateAccessToken(String accessExpiry, String email, String dnos) throws Exception
	{
		try {

		String accessToken;

		if (email == null || dnos == null || email.trim().isEmpty() || dnos.trim().isEmpty()) {
			
			logger.info("Invalid or Missing Credentials");
			return "Invalid or Missing Credentials";
		}

		else {
			String[] dnosArray = dnos.replaceAll("[\\[\\]\\s]", "").split(",");

			if (accessExpiry != null) {

				Integer accessExp = Integer.parseInt(accessExpiry);
				
				accessToken = jwtImplementation.generateAccessToken(accessExp, email, dnosArray);
				
				if(accessToken.equals("false")) {
					
					logger.info("Cannot generate token. Check logs");
					return "Cannot generate token. Check logs";
					
				}
				else {
					logger.info("Token Generated Successfully");
					return accessToken;
				}
				
			}

			else {
				Integer defaultAccessExpiry = 1;
				accessToken = jwtImplementation.generateAccessToken(defaultAccessExpiry, email, dnosArray);
				
				if(accessToken.equals("false")) {
					
					logger.info("Cannot generate token. Check logs");
					return "Cannot generate token. Check logs";
					
				}
				else {
					logger.info("Token Generated Successfully");
					return accessToken;
				}										
			}
		}
	} 
	catch (Exception e) {
		return e.getMessage();

	}
	}
}
