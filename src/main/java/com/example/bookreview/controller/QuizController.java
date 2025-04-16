package com.example.bookreview.controller;

import com.example.bookreview.model.QuizAttempt;
import com.example.bookreview.model.User;
import com.example.bookreview.repository.QuizAttemptRepository;
import com.example.bookreview.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class QuizController {

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/book-trivia-quiz")
    public String showQuiz(HttpSession session, Model model) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }

        // Define quiz questions
        List<Map<String, Object>> questions = new ArrayList<>();
        
        // Question 1
        Map<String, Object> q1 = new HashMap<>();
        q1.put("text", "Who wrote the famous novel 'To Kill a Mockingbird'?");
        q1.put("options", Arrays.asList("Harper Lee", "Mark Twain", "Ernest Hemingway", "F. Scott Fitzgerald"));
        q1.put("correctAnswer", 0);
        questions.add(q1);

        // Question 2
        Map<String, Object> q2 = new HashMap<>();
        q2.put("text", "Which book series features a young wizard named Harry Potter?");
        q2.put("options", Arrays.asList("The Chronicles of Narnia", "The Lord of the Rings", "Harry Potter", "Percy Jackson"));
        q2.put("correctAnswer", 2);
        questions.add(q2);

        // Question 3
        Map<String, Object> q3 = new HashMap<>();
        q3.put("text", "Who is the author of '1984'?");
        q3.put("options", Arrays.asList("George Orwell", "Aldous Huxley", "Ray Bradbury", "Kurt Vonnegut"));
        q3.put("correctAnswer", 0);
        questions.add(q3);

        // Question 4
        Map<String, Object> q4 = new HashMap<>();
        q4.put("text", "Which book features a character named Atticus Finch?");
        q4.put("options", Arrays.asList("The Great Gatsby", "To Kill a Mockingbird", "Of Mice and Men", "The Catcher in the Rye"));
        q4.put("correctAnswer", 1);
        questions.add(q4);

        // Question 5
        Map<String, Object> q5 = new HashMap<>();
        q5.put("text", "Who wrote 'The Great Gatsby'?");
        q5.put("options", Arrays.asList("Ernest Hemingway", "F. Scott Fitzgerald", "John Steinbeck", "William Faulkner"));
        q5.put("correctAnswer", 1);
        questions.add(q5);

        try {
            ObjectMapper mapper = new ObjectMapper();
            model.addAttribute("questions", questions); // Pass the raw questions object
            model.addAttribute("customerName", customerName);
            return "book-trivia-quiz";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/challenges?error=quiz_load_failed";
        }
    }

    @GetMapping("/quiz-results")
    public String showResults(@RequestParam int score, @RequestParam int total, Model model, HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.findByUsername(customerName);
        if (!userOpt.isPresent()) {
            return "redirect:/login";
        }

        QuizAttempt attempt = new QuizAttempt(userOpt.get(), score, total);
        quizAttemptRepository.save(attempt);

        model.addAttribute("score", score);
        model.addAttribute("total", total);
        model.addAttribute("customerName", customerName);
        return "redirect:/quiz-leaderboard";
    }

    @GetMapping("/quiz-leaderboard")
    public String showLeaderboard(Model model, HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }

        List<Object[]> aggregatedScores = quizAttemptRepository.findAggregatedScores();
        Optional<User> currentUser = userService.findByUsername(customerName);
        
        if (currentUser.isPresent()) {
            Integer totalScore = quizAttemptRepository.findTotalScore(currentUser.get());
            Integer bestScore = quizAttemptRepository.getBestScore(currentUser.get());
            Long attemptCount = quizAttemptRepository.getAttemptCount(currentUser.get());
            Double accuracy = quizAttemptRepository.getAverageAccuracy(currentUser.get());
            
            model.addAttribute("totalScore", totalScore != null ? totalScore : 0);
            model.addAttribute("bestScore", bestScore != null ? bestScore : 0);
            model.addAttribute("attemptCount", attemptCount);
            model.addAttribute("accuracy", accuracy != null ? Math.round(accuracy) : 0);
        }

        model.addAttribute("aggregatedScores", aggregatedScores);
        model.addAttribute("customerName", customerName);
        return "quiz-leaderboard";
    }
} 