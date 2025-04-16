package com.example.bookreview.repository;

import com.example.bookreview.model.QuizAttempt;
import com.example.bookreview.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    @Query("SELECT qa.user.username as username, " +
           "SUM(qa.score) as totalScore, " +
           "COUNT(qa) as attempts, " +
           "MAX(qa.score) as bestScore, " +
           "AVG(qa.score * 100.0 / qa.totalQuestions) as accuracy " +
           "FROM QuizAttempt qa " +
           "GROUP BY qa.user.username " +
           "ORDER BY totalScore DESC")
    List<Object[]> findAggregatedScores();

    @Query("SELECT SUM(qa.score) FROM QuizAttempt qa WHERE qa.user = :user")
    Integer findTotalScore(@Param("user") User user);

    @Query("SELECT COUNT(qa) > 0 FROM QuizAttempt qa WHERE qa.user = :user")
    boolean hasAttemptedQuiz(@Param("user") User user);

    @Query("SELECT COUNT(qa) FROM QuizAttempt qa WHERE qa.user = :user")
    long getAttemptCount(@Param("user") User user);

    @Query("SELECT MAX(qa.score) FROM QuizAttempt qa WHERE qa.user = :user")
    Integer getBestScore(@Param("user") User user);

    @Query("SELECT AVG(qa.score * 100.0 / qa.totalQuestions) FROM QuizAttempt qa WHERE qa.user = :user")
    Double getAverageAccuracy(@Param("user") User user);
} 