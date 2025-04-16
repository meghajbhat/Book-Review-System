package com.example.bookreview.repository;

import com.example.bookreview.model.CharacterMatchScore;
import com.example.bookreview.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CharacterMatchScoreRepository extends JpaRepository<CharacterMatchScore, Long> {
    
    @Query("SELECT s.user.username, " +
           "SUM(s.score) as totalScore, " +
           "COUNT(s) as gamesPlayed, " +
           "MAX(s.score) as bestScore, " +
           "MIN(s.attempts) as bestAttempt " +
           "FROM CharacterMatchScore s " +
           "GROUP BY s.user.username " +
           "ORDER BY totalScore DESC")
    List<Object[]> findAggregatedScores();

    @Query("SELECT SUM(s.score) FROM CharacterMatchScore s WHERE s.user = :user")
    Integer findTotalScore(@Param("user") User user);

    @Query("SELECT MAX(s.score) FROM CharacterMatchScore s WHERE s.user = :user")
    Integer findBestScore(@Param("user") User user);

    @Query("SELECT COUNT(s) FROM CharacterMatchScore s WHERE s.user = :user")
    Long countMatchesPlayed(@Param("user") User user);

    @Query("SELECT s FROM CharacterMatchScore s WHERE s.user = :user ORDER BY s.score DESC")
    List<CharacterMatchScore> findUserScores(@Param("user") User user);
} 