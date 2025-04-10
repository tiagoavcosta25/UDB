package org.socialnetwork.service.token;

public interface TokenService {
    String buildToken(String username);
    String verifyToken(String token);
    void verifyAuthenticationHeader(String authorization, String username);
}
