package com.example.bookreview.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class PersonalBingoCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean completed;

    @ElementCollection
    @CollectionTable(name = "personal_bingo_challenges")
    private List<String> challenges;

    @ElementCollection
    @CollectionTable(name = "personal_completed_challenges")
    private List<String> completedChallenges;

    public PersonalBingoCard(User user, String title) {
        this.user = user;
        this.title = title;
        this.createdAt = LocalDateTime.now();
        this.completed = false;
        this.completedChallenges = new ArrayList<>();
    }

    public double getProgress() {
        if (challenges == null || challenges.isEmpty()) return 0.0;
        return (double) completedChallenges.size() / challenges.size() * 100;
    }

    public boolean hasBingo() {
        if (challenges == null || completedChallenges == null || challenges.size() < 9) {
            return false;
        }

        int size = (int) Math.sqrt(challenges.size());
        if (size * size != challenges.size()) {
            size = 3; // Default to 3x3 grid if not a perfect square
        }

        // Check rows
        for (int row = 0; row < size; row++) {
            boolean rowComplete = true;
            for (int col = 0; col < size; col++) {
                String challenge = challenges.get(row * size + col);
                if (!completedChallenges.contains(challenge)) {
                    rowComplete = false;
                    break;
                }
            }
            if (rowComplete) return true;
        }

        // Check columns
        for (int col = 0; col < size; col++) {
            boolean colComplete = true;
            for (int row = 0; row < size; row++) {
                String challenge = challenges.get(row * size + col);
                if (!completedChallenges.contains(challenge)) {
                    colComplete = false;
                    break;
                }
            }
            if (colComplete) return true;
        }

        // Check main diagonal
        boolean mainDiagonalComplete = true;
        for (int i = 0; i < size; i++) {
            String challenge = challenges.get(i * size + i);
            if (!completedChallenges.contains(challenge)) {
                mainDiagonalComplete = false;
                break;
            }
        }
        if (mainDiagonalComplete) return true;

        // Check other diagonal
        boolean otherDiagonalComplete = true;
        for (int i = 0; i < size; i++) {
            String challenge = challenges.get(i * size + (size - 1 - i));
            if (!completedChallenges.contains(challenge)) {
                otherDiagonalComplete = false;
                break;
            }
        }
        return otherDiagonalComplete;
    }
} 