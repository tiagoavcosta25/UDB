package com.example.udb.service;

import com.couchbase.client.core.deps.io.netty.util.CharsetUtil;
import com.couchbase.client.java.json.JsonObject;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class TokenService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.enabled}")
    private boolean useJwt;

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
        String token = Jwts.builder().signWith(SignatureAlgorithm.HS512, secret)
                .setPayload(JsonObject.create()
                        .put("user", username)
                        .toString())
                .compact();

        return token;
    }

    private String buildSimpleToken(String username) {
        return Base64.getEncoder().encodeToString(username.getBytes(CharsetUtil.UTF_8));
    }
}
