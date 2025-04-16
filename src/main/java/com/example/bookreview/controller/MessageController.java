package com.example.bookreview.controller;

import com.example.bookreview.model.Message;
import com.example.bookreview.model.User;
import com.example.bookreview.service.MessageService;
import com.example.bookreview.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getMessages(@PathVariable Long userId, HttpSession session) {
        logger.debug("Getting messages for userId: {}", userId);
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            logger.error("User not logged in");
            return ResponseEntity.badRequest().body("Not logged in");
        }

        User currentUser = userService.findByUsername(customerName).orElse(null);
        User otherUser = userService.getUserById(userId);

        if (currentUser == null || otherUser == null) {
            logger.error("User not found. Current user: {}, Other user: {}", currentUser, otherUser);
            return ResponseEntity.badRequest().body("User not found");
        }

        List<Message> messages = messageService.getMessagesBetweenUsers(currentUser, otherUser);
        logger.debug("Found {} messages between users", messages.size());
        
        List<Map<String, Object>> messageDTOs = messages.stream()
            .map(message -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("content", message.getContent());
                dto.put("sent", message.getSender().equals(currentUser));
                dto.put("timestamp", message.getSentAt().toString());
                return dto;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(messageDTOs);
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, Object> request, HttpSession session) {
        logger.debug("Received message request: {}", request);
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            logger.error("User not logged in");
            return ResponseEntity.badRequest().body("Not logged in");
        }

        try {
            User sender = userService.findByUsername(customerName).orElse(null);
            User receiver = userService.getUserById(Long.parseLong(request.get("receiverId").toString()));
            String content = (String) request.get("content");

            if (sender == null || receiver == null || content == null || content.trim().isEmpty()) {
                logger.error("Invalid request. Sender: {}, Receiver: {}, Content: {}", sender, receiver, content);
                return ResponseEntity.badRequest().body("Invalid request");
            }

            logger.debug("Sending message from {} to {}: {}", sender.getUsername(), receiver.getUsername(), content);
            Message message = messageService.sendMessage(sender, receiver, content);
            logger.debug("Message sent successfully with id: {}", message.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("content", message.getContent());
            response.put("sent", true);
            response.put("timestamp", message.getSentAt().toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error sending message: ", e);
            return ResponseEntity.badRequest().body("Error sending message: " + e.getMessage());
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return ResponseEntity.badRequest().body("Not logged in");
        }

        User currentUser = userService.findByUsername(customerName).orElse(null);
        if (currentUser == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        long unreadCount = messageService.getUnreadMessageCount(currentUser);
        return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
    }
} 