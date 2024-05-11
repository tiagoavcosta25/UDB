package com.example.udb.controller;

import com.couchbase.client.core.msg.kv.DurabilityLevel;
import com.couchbase.client.java.json.JsonObject;
import com.example.udb.service.UserService;
import com.example.udb.util.Error;
import com.example.udb.util.IValue;
import com.example.udb.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tenants")
public class UserController {
    private final UserService userService;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Value("${storage.expiry:0}")
    private int durabilityLevel;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "/{tenant}/user/login", method = RequestMethod.POST)
    public ResponseEntity<? extends IValue> login(@PathVariable("tenant") String tenant, @RequestBody Map<String, String> loginInfo) {
        String user = loginInfo.get("user");
        String password = loginInfo.get("password");
        if (user == null || password == null) {
            return ResponseEntity.badRequest().body(new Error("User or password missing, or malformed request"));
        }

        try {
            return ResponseEntity.ok(userService.login(tenant, user, password));
        } catch (AuthenticationServiceException e) {
            e.printStackTrace();
            LOGGER.error("Authentication failed with exception", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Error(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Failed with exception", e);
            return ResponseEntity.status(500).body(new Error(e.getMessage()));
        }
    }

    @RequestMapping(value = "/{tenant}/user/signup", method = RequestMethod.POST)
    public ResponseEntity<? extends IValue> register(@PathVariable("tenant") String tenant, @RequestBody String json) {
        JsonObject jsonData = JsonObject.fromJson(json);
        try {
            Result<Map<String, Object>> result = userService.register(tenant, jsonData.getString("user"), jsonData.getString("password"), DurabilityLevel.values()[durabilityLevel]);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (AuthenticationServiceException e) {
            e.printStackTrace();
            LOGGER.error("Authentication failed with exception", e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new Error(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Failed with exception", e);
            return ResponseEntity.status(500).body(new Error(e.getMessage()));
        }
    }
}
