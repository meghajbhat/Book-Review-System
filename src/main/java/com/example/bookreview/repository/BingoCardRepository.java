package com.example.bookreview.repository;

import com.example.bookreview.model.BingoCard;
import com.example.bookreview.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface BingoCardRepository extends JpaRepository<BingoCard, Long> {
    List<BingoCard> findByUserOrderByCreatedAtDesc(User user);
    
    Optional<BingoCard> findFirstByUserAndCompletedFalseOrderByCreatedAtDesc(User user);
    
    @Query("SELECT COUNT(b) FROM BingoCard b WHERE b.user = :user AND b.completed = true")
    long countCompletedCards(User user);
    
    @Query("SELECT b FROM BingoCard b WHERE b.user = :user AND b.completed = false ORDER BY b.createdAt DESC")
    List<BingoCard> findActiveCards(@Param("user") User user);
} 