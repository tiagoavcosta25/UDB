package org.socialnetwork.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.repository.config.EnableCouchbaseRepositories;
import org.springframework.data.couchbase.repository.config.EnableReactiveCouchbaseRepositories;

@Configuration
@EnableCouchbaseRepositories
@EnableReactiveCouchbaseRepositories
public class Database extends AbstractCouchbaseConfiguration {
    @Value("${storage.host}") private String host;

    @Value("${storage.bucket}") private String bucket;

    @Value("${storage.username}") private String username;

    @Value("${storage.password}") private String password;

    @Override
    public String getConnectionString() {
        return host;
    }

    @Override
    public String getUserName() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getBucketName() {
        return bucket;
    }
}
