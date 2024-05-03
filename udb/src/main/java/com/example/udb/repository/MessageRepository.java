package com.example.udb.repository;

import com.example.udb.model.Message;
import org.springframework.data.couchbase.repository.CouchbaseRepository;

public interface MessageRepository extends CouchbaseRepository<Message, String> {
}
