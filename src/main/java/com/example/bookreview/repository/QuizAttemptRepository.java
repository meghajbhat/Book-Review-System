package com.example.bookreview.repository;

import com.example.bookreview.model.QuizAttempt;
import com.example.bookreview.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    @Query("SELECT qa FROM QuizAttempt qa ORDER BY qa.score DESC, qa.attemptDate DESC")
    List<QuizAttempt> findTopScores();

    @Query("SELECT COUNT(qa) > 0 FROM QuizAttempt qa WHERE qa.user = :user")
    boolean hasAttemptedQuiz(@Param("user") User user);

    @Query("SELECT COUNT(qa) FROM QuizAttempt qa WHERE qa.user = :user")
    long getAttemptCount(@Param("user") User user);

    @Query("SELECT MAX(qa.score) FROM QuizAttempt qa WHERE qa.user = :user")
    Integer getBestScore(@Param("user") User user);
} 