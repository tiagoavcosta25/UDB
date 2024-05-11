package com.example.udb.service.token;

public interface ITokenService {
    String buildToken(String username);
    String verifyToken(String token);
    void verifyAuthenticationHeader(String authorization, String username);
}
