package com.example.bookreview.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "detective_cases")
public class DetectiveCase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    private String caseName;
    private String clue1;
    private String clue2;
    private String clue3;
    private String solution;
    private boolean solved;
    private int attemptsUsed;
    private int pointsEarned;
    private LocalDateTime startTime;
    private LocalDateTime solvedTime;
    private LocalDateTime endTime;
    private boolean completed;
    private int points;
    private String mystery;
    private String clues;

    public DetectiveCase() {
    }

    public DetectiveCase(User user, Book book, String caseName, String clue1, String clue2, String clue3, String solution) {
        this.user = user;
        this.book = book;
        this.caseName = caseName;
        this.clue1 = clue1;
        this.clue2 = clue2;
        this.clue3 = clue3;
        this.solution = solution;
        this.solved = false;
        this.attemptsUsed = 0;
        this.pointsEarned = 0;
        this.startTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public String getCaseName() {
        return caseName;
    }

    public void setCaseName(String caseName) {
        this.caseName = caseName;
    }

    public String getClue1() {
        return clue1;
    }

    public void setClue1(String clue1) {
        this.clue1 = clue1;
    }

    public String getClue2() {
        return clue2;
    }

    public void setClue2(String clue2) {
        this.clue2 = clue2;
    }

    public String getClue3() {
        return clue3;
    }

    public void setClue3(String clue3) {
        this.clue3 = clue3;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public boolean isSolved() {
        return solved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
        if (solved) {
            this.solvedTime = LocalDateTime.now();
        }
    }

    public int getAttemptsUsed() {
        return attemptsUsed;
    }

    public void setAttemptsUsed(int attemptsUsed) {
        this.attemptsUsed = attemptsUsed;
    }

    public int getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(int pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getSolvedTime() {
        return solvedTime;
    }

    public void setSolvedTime(LocalDateTime solvedTime) {
        this.solvedTime = solvedTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getMystery() {
        return mystery;
    }

    public void setMystery(String mystery) {
        this.mystery = mystery;
    }

    public String getClues() {
        return clues;
    }

    public void setClues(String clues) {
        this.clues = clues;
    }
} 