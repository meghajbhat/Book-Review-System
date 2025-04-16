package com.example.bookreview.service;

import com.example.bookreview.model.CharacterMatchScore;
import com.example.bookreview.model.User;
import com.example.bookreview.model.Book;
import com.example.bookreview.repository.CharacterMatchScoreRepository;
import com.example.bookreview.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class CharacterMatchService {

    @Autowired
    private CharacterMatchScoreRepository scoreRepository;

    @Autowired
    private BookRepository bookRepository;

    public CharacterMatchScore startNewGame(User user) {
        // Get a random book
        List<Book> books = bookRepository.findAll();
        if (books.isEmpty()) {
            throw new RuntimeException("No books available for the game");
        }

        Random random = new Random();
        Book selectedBook = books.get(random.nextInt(books.size()));

        // Create a new score entry
        CharacterMatchScore score = new CharacterMatchScore();
        score.setUser(user);
        score.setScore(0);
        score.setAttempts(0);
        score.setBookTitle(selectedBook.getTitle());
        score.setStartTime(LocalDateTime.now());
        
        return scoreRepository.save(score);
    }

    public String scrambleTitle(String title) {
        List<Character> chars = new ArrayList<>();
        for (char c : title.toCharArray()) {
            chars.add(c);
        }
        Collections.shuffle(chars);
        StringBuilder scrambled = new StringBuilder();
        for (Character c : chars) {
            scrambled.append(c);
        }
        return scrambled.toString();
    }

    public String checkAnswer(String answer, Long scoreId) {
        CharacterMatchScore score = scoreRepository.findById(scoreId)
            .orElseThrow(() -> new RuntimeException("Score not found"));

        score.setAttempts(score.getAttempts() + 1);

        if (answer.trim().equalsIgnoreCase(score.getBookTitle().trim())) {
            int points = calculatePoints(score.getAttempts());
            score.setScore(points);
            scoreRepository.save(score);
            return "Correct! You solved it in " + score.getAttempts() + " attempts! Points: " + points;
        } else {
            scoreRepository.save(score);
            return "Incorrect. Try again! Attempts: " + score.getAttempts();
        }
    }

    public int calculatePoints(int attempts) {
        // Base points: 100
        // Deduct 10 points for each attempt after the first
        return Math.max(20, 100 - (attempts - 1) * 10);
    }

    public List<Object[]> getLeaderboard() {
        return scoreRepository.findAggregatedScores();
    }

    public Integer getTotalScore(User user) {
        return scoreRepository.findTotalScore(user);
    }

    public Integer getBestScore(User user) {
        return scoreRepository.findBestScore(user);
    }

    public Long getMatchesPlayed(User user) {
        return scoreRepository.countMatchesPlayed(user);
    }
} 