package com.example.bookreview.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class BingoCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean completed;

    @ElementCollection
    @CollectionTable(name = "bingo_challenges")
    private List<String> challenges;

    @ElementCollection
    @CollectionTable(name = "completed_challenges")
    private List<String> completedChallenges;

    public BingoCard(User user) {
        this.user = user;
        this.createdAt = LocalDateTime.now();
        this.completed = false;
        this.completedChallenges = new ArrayList<>();
    }

    public double getProgress() {
        if (challenges == null || challenges.isEmpty()) return 0.0;
        return (double) completedChallenges.size() / challenges.size() * 100;
    }

    public boolean hasBingo() {
        if (challenges == null || completedChallenges == null || challenges.size() != 25) {
            return false;
        }

        // Check rows
        for (int row = 0; row < 5; row++) {
            boolean rowComplete = true;
            for (int col = 0; col < 5; col++) {
                String challenge = challenges.get(row * 5 + col);
                if (!completedChallenges.contains(challenge)) {
                    rowComplete = false;
                    break;
                }
            }
            if (rowComplete) return true;
        }

        // Check columns
        for (int col = 0; col < 5; col++) {
            boolean colComplete = true;
            for (int row = 0; row < 5; row++) {
                String challenge = challenges.get(row * 5 + col);
                if (!completedChallenges.contains(challenge)) {
                    colComplete = false;
                    break;
                }
            }
            if (colComplete) return true;
        }

        // Check main diagonal (top-left to bottom-right)
        boolean mainDiagonalComplete = true;
        for (int i = 0; i < 5; i++) {
            String challenge = challenges.get(i * 5 + i);
            if (!completedChallenges.contains(challenge)) {
                mainDiagonalComplete = false;
                break;
            }
        }
        if (mainDiagonalComplete) return true;

        // Check other diagonal (top-right to bottom-left)
        boolean otherDiagonalComplete = true;
        for (int i = 0; i < 5; i++) {
            String challenge = challenges.get(i * 5 + (4 - i));
            if (!completedChallenges.contains(challenge)) {
                otherDiagonalComplete = false;
                break;
            }
        }
        return otherDiagonalComplete;
    }
} 