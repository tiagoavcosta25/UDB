package com.example.udb.service;

import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.core.msg.kv.DurabilityLevel;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.UpsertOptions;
import com.example.udb.model.User;
import com.example.udb.repository.IUserRepository;
import com.example.udb.service.token.ITokenService;
import org.framework.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    private final ITokenService jwtService;
    private final IUserRepository IUserRepository;

    @Autowired
    public UserService(ITokenService tokenService, IUserRepository IUserRepository) {
        this.jwtService = tokenService;
        this.IUserRepository = IUserRepository;
    }

    public Result<Map<String, Object>> register(final String tenant, final String username, final String password, DurabilityLevel durabilityLevel) {
        if (!StringUtils.hasLength(username) || !StringUtils.hasLength(password) || password.length() < 3) {
            throw new AuthenticationServiceException("Invalid username or password");
        }

        IUserRepository IUserRepository = this.IUserRepository.withScope(tenant);
        UpsertOptions options = UpsertOptions.upsertOptions();
        options.durability(durabilityLevel);

        String passHash = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User(username, passHash);

        String queryType = String.format("KV insert - scoped to %s.users: document %s", tenant, username);

        try {
            IUserRepository.withOptions(options).save(user);
            Map<String, Object> data = JsonObject.create().put("token", jwtService.buildToken(username)).toMap();

            return Result.of(data, queryType);
        } catch (DataAccessException e) {
            throw new AuthenticationServiceException("Database access error during account creation", e);
        } catch (Exception e) {
            throw new AuthenticationServiceException("There was an unexpected error creating the account", e);
        }
    }

    public Result<Map<String, Object>> login(final String tenant, final String username, final String password) {
        IUserRepository IUserRepository = this.IUserRepository.withScope(tenant);
        String queryType = String.format("KV get - scoped to %s.users: for password field in document %s", tenant, username);
        Optional<User> userHolder;
        try {
            userHolder = IUserRepository.findById(username);
        } catch (DocumentNotFoundException e) {
            throw new AuthenticationCredentialsNotFoundException("Bad Username or Password");
        }
        User res = userHolder.get();
        if (BCrypt.checkpw(password, res.password)) {
            Map<String, Object> data = JsonObject.create().put("token", jwtService.buildToken(username)).toMap();
            return Result.of(data, queryType);
        } else {
            throw new AuthenticationCredentialsNotFoundException("Bad Username or Password");
        }
    }
}
