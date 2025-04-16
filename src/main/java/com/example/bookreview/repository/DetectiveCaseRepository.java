package com.example.bookreview.repository;

import com.example.bookreview.model.DetectiveCase;
import com.example.bookreview.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DetectiveCaseRepository extends JpaRepository<DetectiveCase, Long> {
    List<DetectiveCase> findByUserOrderByStartTimeDesc(User user);
    
    @Query("SELECT COUNT(d) FROM DetectiveCase d WHERE d.user = :user AND d.solved = true")
    long countSolvedCases(User user);
    
    @Query("SELECT COALESCE(SUM(d.pointsEarned), 0) FROM DetectiveCase d WHERE d.user = :user")
    Integer getTotalPoints(User user);
    
    @Query("SELECT d.user.username, SUM(d.pointsEarned), COUNT(CASE WHEN d.solved = true THEN 1 END) " +
           "FROM DetectiveCase d " +
           "GROUP BY d.user.username " +
           "ORDER BY SUM(d.pointsEarned) DESC")
    List<Object[]> findTopDetectivesByPoints();
    
    @Query("SELECT dc FROM DetectiveCase dc WHERE dc.user = :user AND dc.solved = false ORDER BY dc.startTime DESC")
    List<DetectiveCase> findUnsolvedCases(@Param("user") User user);

    List<DetectiveCase> findByBookId(Long bookId);
} 