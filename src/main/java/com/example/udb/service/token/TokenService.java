package com.example.udb.service.token;

import com.couchbase.client.core.deps.io.netty.util.CharsetUtil;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Base64;
import java.util.Date;

@Deprecated
@Service
public class TokenService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.enabled:true}")
    private boolean useJwt;

    @Value("${jwt.algorithm:HS256}")
    private SignatureAlgorithm algorithm;

    @Value("${jwt.tokenExpiry}:3600")
    private Integer tokenExpiry;

    /**
     * @throws IllegalStateException when the Authorization header couldn't be verified or didn't match the
     * expected username
     */
    public void verifyAuthenticationHeader(String authorization, String expectedUsername) {
        String token = authorization.replaceFirst("Bearer ", "");
        String tokenName;
        if (useJwt) {
            tokenName = verifyJwt(token);
        } else {
            tokenName = verifySimple(token);
        }
        if (!expectedUsername.equals(tokenName)) {
            throw new IllegalStateException("Token and username don't match");
        }
    }

    public String verifyJwt(String token) {
        try {
            String username = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody()
                    .get("user", String.class);
            return username;
        } catch (JwtException e) {
            throw new IllegalStateException("Could not verify JWT token", e);
        }
    }

    public String verifySimple(String token) {
        try {
            return new String(Base64.getDecoder().decode(token));
        } catch (Exception e) {
            throw new IllegalStateException("Could not verify simple token", e);
        }
    }

    public String buildToken(String username) {
        if (useJwt) {
            return buildJwtToken(username);
        } else {
            return buildSimpleToken(username);
        }
    }

    public String buildJwtToken(String username) {
        Duration tokenExpiryDuration = Duration.ofMinutes(tokenExpiry);
        Date tokenExpiry = new Date(System.currentTimeMillis() + tokenExpiryDuration.toMillis());

        return Jwts.builder()
                .signWith(algorithm, secret)
                .setClaims(Jwts.claims().setSubject(username))
                .setIssuedAt(new Date())
                .setExpiration(tokenExpiry)
                .compact();
    }

    private String buildSimpleToken(String username) {
        return Base64.getEncoder().encodeToString(username.getBytes(CharsetUtil.UTF_8));
    }
}
