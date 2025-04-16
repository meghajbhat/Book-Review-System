package com.example.bookreview.service;

import com.example.bookreview.model.Message;
import com.example.bookreview.model.User;
import com.example.bookreview.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Comparator;

@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;

    public Message sendMessage(User sender, User receiver, String content) {
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        message.setSentAt(LocalDateTime.now());
        return messageRepository.save(message);
    }

    public List<Message> getMessagesBetweenUsers(User user1, User user2) {
        List<Message> messages = messageRepository.findMessagesBetweenUsers(user1, user2);
        // Mark messages as read
        messages.stream()
            .filter(m -> m.getReceiver().equals(user1) && !m.isRead())
            .forEach(m -> {
                m.setRead(true);
                messageRepository.save(m);
            });
        return messages;
    }

    public long getUnreadMessageCount(User user) {
        return messageRepository.countUnreadMessages(user);
    }

    public List<Message> getUnreadMessages(User user) {
        return messageRepository.findByReceiverAndReadFalse(user);
    }

    public List<Map<String, Object>> getConversations(User user) {
        // Get all messages where the user is either sender or receiver
        List<Message> allMessages = messageRepository.findMessagesByUser(user);
        
        // Group messages by the other user in the conversation
        Map<Long, List<Message>> messagesByUser = new HashMap<>();
        for (Message message : allMessages) {
            Long otherUserId = message.getSender().equals(user) ? 
                message.getReceiver().getUserId() : message.getSender().getUserId();
            messagesByUser.computeIfAbsent(otherUserId, k -> new ArrayList<>()).add(message);
        }

        // Create conversation summaries
        List<Map<String, Object>> conversations = new ArrayList<>();
        for (Map.Entry<Long, List<Message>> entry : messagesByUser.entrySet()) {
            Long otherUserId = entry.getKey();
            List<Message> messages = entry.getValue();
            Message lastMessage = messages.stream()
                .max(Comparator.comparing(Message::getSentAt))
                .orElse(null);

            if (lastMessage != null) {
                User otherUser = lastMessage.getSender().equals(user) ? 
                    lastMessage.getReceiver() : lastMessage.getSender();

                Map<String, Object> conversation = new HashMap<>();
                conversation.put("userId", otherUserId);
                conversation.put("name", otherUser.getName());
                conversation.put("lastMessage", lastMessage.getContent());
                conversation.put("lastMessageTime", formatTime(lastMessage.getSentAt()));
                conversation.put("unreadCount", messages.stream()
                    .filter(m -> m.getReceiver().equals(user) && !m.isRead())
                    .count());

                conversations.add(conversation);
            }
        }

        // Sort conversations by last message time (most recent first)
        conversations.sort((c1, c2) -> {
            String time1 = (String) c1.get("lastMessageTime");
            String time2 = (String) c2.get("lastMessageTime");
            return time2.compareTo(time1);
        });

        return conversations;
    }

    private String formatTime(LocalDateTime time) {
        LocalDateTime now = LocalDateTime.now();
        if (time.toLocalDate().equals(now.toLocalDate())) {
            return time.format(DateTimeFormatter.ofPattern("HH:mm"));
        } else if (time.toLocalDate().equals(now.minusDays(1).toLocalDate())) {
            return "Yesterday";
        } else {
            return time.format(DateTimeFormatter.ofPattern("MMM d"));
        }
    }
} 