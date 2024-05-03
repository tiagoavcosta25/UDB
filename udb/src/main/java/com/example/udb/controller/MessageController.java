package com.example.udb.controller;

import com.example.udb.model.Message;
import com.example.udb.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    @GetMapping("/{id}")
    public Message getMessage(@PathVariable String id) {
        return messageRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Message createMessage(@RequestBody Message message) {
        return messageRepository.save(message);
    }

    @PutMapping("/{id}")
    public Message updateMessage(@PathVariable String id, @RequestBody Message message) {
        message.setId(id);
        return messageRepository.save(message);
    }

    @DeleteMapping("/{id}")
    public void deleteMessage(@PathVariable String id) {
        messageRepository.deleteById(id);
    }
}