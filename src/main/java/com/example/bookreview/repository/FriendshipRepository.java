package com.example.bookreview.repository;

import com.example.bookreview.model.Friendship;
import com.example.bookreview.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    List<Friendship> findByReceiverAndStatus(User receiver, Friendship.FriendshipStatus status);
    List<Friendship> findByRequesterAndStatus(User requester, Friendship.FriendshipStatus status);
    
    List<Friendship> findByRequesterAndReceiverAndStatusIn(User requester, User receiver, List<Friendship.FriendshipStatus> statuses);
    
    @Query("SELECT f FROM Friendship f WHERE (f.requester = :user OR f.receiver = :user) AND f.status = 'ACCEPTED'")
    List<Friendship> findFriends(@Param("user") User user);
    
    @Query("SELECT f FROM Friendship f WHERE ((f.requester = :user1 AND f.receiver = :user2) OR (f.requester = :user2 AND f.receiver = :user1)) AND f.status = 'ACCEPTED'")
    Friendship findFriendshipBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);

    List<Friendship> findByRequesterOrReceiverAndStatus(User requester, User receiver, Friendship.FriendshipStatus status);
} 