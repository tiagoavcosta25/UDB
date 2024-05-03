package com.example.udb.service;

import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.core.msg.kv.DurabilityLevel;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.UpsertOptions;
import com.example.udb.model.User;
import com.example.udb.repository.UserRepository;
import com.example.udb.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    private final TokenService jwtService;
    private final UserRepository userRepository;

    @Autowired
    public UserService(TokenService tokenService, UserRepository userRepository) {
        this.jwtService = tokenService;
        this.userRepository = userRepository;
    }

    public Result<Map<String, Object>> login(final String tenant, final String username, final String password) {
        UserRepository userRepository = this.userRepository.withScope(tenant);
        String queryType = String.format("KV get - scoped to %s.users: for password field in document %s", tenant, username);
        Optional<User> userHolder;
        try {
            userHolder = userRepository.findById(username);
        } catch (DocumentNotFoundException e) {
            throw new AuthenticationCredentialsNotFoundException("Bad Username or Password");
        }
        User res = userHolder.get();
        if (BCrypt.checkpw(password, res.password)) {
            Map<String, Object> data = JsonObject.create().put("token", jwtService.buildJwtToken(username)).toMap();
            return Result.of(data, queryType);
        } else {
            throw new AuthenticationCredentialsNotFoundException("Bad USername or Password");
        }
    }

    public Result<Map<String, Object>> createLogin(final String tenant, final String username, final String password, DurabilityLevel expiry) {
        UserRepository userRepository = this.userRepository.withScope(tenant);
        String passHash = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User(username, passHash);
        UpsertOptions options = UpsertOptions.upsertOptions();
        if (expiry.ordinal() > 0) {
            options.durability(expiry);
        }
        String queryType = String.format("KV insert - scoped to %s.users: document %s", tenant, username);
        try {
            userRepository.withOptions(options).save(user);
            Map<String, Object> data = JsonObject.create().put("token", jwtService.buildToken(username)).toMap();
            return Result.of(data, queryType);
        } catch (Exception e) {
            throw new AuthenticationServiceException("There was an error creating account");
        }
    }
}
