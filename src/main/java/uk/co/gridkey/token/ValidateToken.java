package uk.co.gridkey.token;

public class ValidateToken {

	private static JWTImplementation jwtImplementation = new JWTImplementation();

	public static Boolean validateToken(String accessToken, String email, String dno) throws Exception

	{
		if (accessToken == null || dno == null || email == null || accessToken.trim().isEmpty() || dno.trim().isEmpty() || email.trim().isEmpty()) {
			return false;
			
		}
		else {
			if (jwtImplementation.validateToken(accessToken, email, dno)) {
				System.out.println("Token is Valid");
				return true;
				
			} else {
				
				return false;
			}
			
		}
	}

}
