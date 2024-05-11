package com.example.udb.service.token;

import com.couchbase.client.core.deps.io.netty.util.CharsetUtil;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service("simpleTokenService")
public class SimpleTokenService implements ITokenService {

    @Override
    public String buildToken(String username) {
        return Base64.getEncoder().encodeToString(username.getBytes(CharsetUtil.UTF_8));
    }

    @Override
    public String verifyToken(String token) {
        try {
            return new String(Base64.getDecoder().decode(token));
        } catch (Exception e) {
            throw new IllegalStateException("Could not verify simple token", e);
        }
    }

    @Override
    public void verifyAuthenticationHeader(String authorization, String username) {
        String token = authorization.replaceFirst("Bearer ", "");
        String tokenName;
        tokenName = verifyToken(token);
        if (!username.equals(tokenName)) {
            throw new IllegalStateException("Token and username don't match");
        }
    }
}
