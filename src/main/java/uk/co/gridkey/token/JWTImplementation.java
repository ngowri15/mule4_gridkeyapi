package uk.co.gridkey.token;

import java.io.IOException;
import java.util.Date;

import com.google.gson.Gson;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JWTImplementation {

	Date currentDate = new Date();

	public String generateAccessToken(int accessExpiry, String email, String[] dnos) throws IOException {

		try {
		Date expiryDate = new Date(currentDate.getTime() + SecurityConstants.getJWTAccessTokenExpiration(accessExpiry));

		Claims claims = Jwts.claims();

		String dnosJson = new Gson().toJson(dnos);
		claims.put("dnos", dnosJson);

		String accessToken = Jwts.builder().setClaims(claims).setSubject(email).setIssuedAt(new Date())
				.setExpiration(expiryDate).signWith(SignatureAlgorithm.HS256, SecurityConstants.getSecret()).compact();

		System.out.println("Access Token Expiry Date: " + expiryDate);

		return accessToken;
		}
		catch(Exception e) {
			
			System.out.println(e);
			return "false";
		}
		
		
	}

	public boolean validateToken(String token, String email, String dno) throws IOException {

		try {
		Jws<Claims> jws = Jwts.parser().setSigningKey(SecurityConstants.getSecret()).parseClaimsJws(token);
		Claims claims = jws.getBody();
		String dnosJson = claims.get("dnos", String.class);
		

		String tokenEmail = claims.get("sub", String.class);

		String[] dnosArray = new Gson().fromJson(dnosJson, String[].class);
		String dnosAdmin = dnosJson.replaceAll("[\\[\\]\"]", "");

		boolean status = false;

		if (tokenEmail.equals(email)) {

			int i = 0;

			if (dnosAdmin.equals("ALL")) {
				status = true;
			} else {
				for (i = 0; i < dnosArray.length; i++) {

					dnosArray[i] = dnosArray[i].replaceAll("^\"|\"$", "");

					if (dnosArray[i].equals(dno)) {
						status = true;
						break;
					}

				}

			}
		}
		return status;

	
	}
	catch(Exception e) {
		
		System.out.println(e.getMessage());
		return false;
	}

}
}