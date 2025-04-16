package com.bookreview.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class QuizController {

    @GetMapping("/book-trivia-quiz")
    public String showQuiz() {
        return "book-trivia-quiz";
    }

    @GetMapping("/quiz-results")
    public String showResults(@RequestParam int score, @RequestParam int total, Model model) {
        model.addAttribute("score", score);
        model.addAttribute("total", total);
        return "quiz-results";
    }

    @GetMapping("/quiz-leaderboard")
    public String showLeaderboard() {
        return "quiz-leaderboard";
    }
} 