package com.example.bookreview.service;

import com.example.bookreview.model.Friendship;
import com.example.bookreview.model.User;
import com.example.bookreview.repository.FriendshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FriendshipService {
    @Autowired
    private FriendshipRepository friendshipRepository;

    public void sendFriendRequest(User requester, User receiver) {
        // Check if a friendship (pending or accepted) already exists
        List<Friendship> existingRequests = friendshipRepository.findByRequesterAndReceiverAndStatusIn(
            requester, receiver, List.of(Friendship.FriendshipStatus.PENDING, Friendship.FriendshipStatus.ACCEPTED));
        
        // Also check the reverse direction
        List<Friendship> reverseRequests = friendshipRepository.findByRequesterAndReceiverAndStatusIn(
            receiver, requester, List.of(Friendship.FriendshipStatus.PENDING, Friendship.FriendshipStatus.ACCEPTED));
        
        // If no existing requests found, create a new one
        if (existingRequests.isEmpty() && reverseRequests.isEmpty()) {
            Friendship friendship = new Friendship();
            friendship.setRequester(requester);
            friendship.setReceiver(receiver);
            friendship.setStatus(Friendship.FriendshipStatus.PENDING);
            friendship.setCreatedAt(LocalDateTime.now());
            friendshipRepository.save(friendship);
        }
    }

    public void acceptFriendRequest(Friendship friendship) {
        friendship.setStatus(Friendship.FriendshipStatus.ACCEPTED);
        friendshipRepository.save(friendship);
    }

    public void rejectFriendRequest(Friendship friendship) {
        friendship.setStatus(Friendship.FriendshipStatus.REJECTED);
        friendshipRepository.save(friendship);
    }

    public List<Friendship> getPendingFriendRequests(User user) {
        List<Friendship> receivedRequests = friendshipRepository.findByReceiverAndStatus(user, Friendship.FriendshipStatus.PENDING);
        List<Friendship> sentRequests = friendshipRepository.findByRequesterAndStatus(user, Friendship.FriendshipStatus.PENDING);
        receivedRequests.addAll(sentRequests);
        return receivedRequests;
    }

    public List<Friendship> getFriends(User user) {
        return friendshipRepository.findFriends(user);
    }

    public Friendship getFriendshipBetweenUsers(User user1, User user2) {
        return friendshipRepository.findFriendshipBetweenUsers(user1, user2);
    }

    public Friendship getFriendshipById(Long id) {
        Optional<Friendship> friendship = friendshipRepository.findById(id);
        return friendship.orElse(null);
    }

    public void removeFriend(User user1, User user2) {
        Friendship friendship = friendshipRepository.findFriendshipBetweenUsers(user1, user2);
        if (friendship != null) {
            friendshipRepository.delete(friendship);
        }
    }

    public List<Friendship> getReceivedFriendRequests(User user) {
        return friendshipRepository.findByReceiverAndStatus(user, Friendship.FriendshipStatus.PENDING);
    }

    public List<Friendship> getSentFriendRequests(User user) {
        return friendshipRepository.findByRequesterAndStatus(user, Friendship.FriendshipStatus.PENDING);
    }

    public List<Friendship> findAcceptedFriendships(User user) {
        return friendshipRepository.findByRequesterOrReceiverAndStatus(user, user, Friendship.FriendshipStatus.ACCEPTED);
    }
} 