package com.example.bookreview.repository;

import com.example.bookreview.model.PersonalBingoCard;
import com.example.bookreview.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PersonalBingoCardRepository extends JpaRepository<PersonalBingoCard, Long> {
    List<PersonalBingoCard> findByUserOrderByCreatedAtDesc(User user);
    
    @Query("SELECT COUNT(b) FROM PersonalBingoCard b WHERE b.user = :user AND b.completed = true")
    long countCompletedCards(@Param("user") User user);
    
    List<PersonalBingoCard> findByUserAndCompletedFalseOrderByCreatedAtDesc(User user);
} 