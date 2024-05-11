package com.example.udb.service.token;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.util.Date;

public class JwtTokenService implements ITokenService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.algorithm:HS256}")
    private SignatureAlgorithm algorithm;

    @Value("${jwt.tokenExpiry}:3600")
    private Integer tokenExpiry;

    @Override
    public String buildToken(String username) {
        Duration tokenExpiryDuration = Duration.ofMinutes(tokenExpiry);
        Date tokenExpiry = new Date(System.currentTimeMillis() + tokenExpiryDuration.toMillis());

        return Jwts.builder()
                .signWith(algorithm, secret)
                .setClaims(Jwts.claims().setSubject(username))
                .setIssuedAt(new Date())
                .setExpiration(tokenExpiry)
                .compact();
    }

    @Override
    public String verifyToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody()
                    .get("user", String.class);
        } catch (JwtException e) {
            throw new IllegalStateException("Could not verify JWT token", e);
        }
    }

    @Override
    public void verifyAuthenticationHeader(String authorization, String username) {
        String token = authorization.replaceFirst("Bearer ", "");
        String tokenName = verifyToken(token);
        if (!username.equals(tokenName)) {
            throw new IllegalStateException("Token and username don't match");
        }
    }
}
