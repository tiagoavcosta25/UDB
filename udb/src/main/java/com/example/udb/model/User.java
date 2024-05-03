package com.example.udb.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.couchbase.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Document
@TypeAlias("user")
public class User {
    @Id
    public String username;
    public String password;

    public User() {
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    // Return object will be a Map
    // Can swap to DTO later
    public Map<String, Object> toMap() {
        // 2 items, closest to the power of 2 is 4 (needs to be bigger than 2)
        // load factor of 0.75, 4*0.75 = 3
        // HashMap will hold 3 items before resizing to double
        Map<String, Object> map = new HashMap<>(4);
        map.put("username", username);
        map.put("password", password);
        return map;
    }
}
