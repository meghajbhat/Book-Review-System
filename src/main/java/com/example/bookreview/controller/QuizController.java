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

import java.util.List;
import java.util.Optional;

@Controller
public class QuizController {

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/book-trivia-quiz")
    public String showQuiz(HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }
        return "book-trivia-quiz";
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
        return "quiz-results";
    }

    @GetMapping("/quiz-leaderboard")
    public String showLeaderboard(Model model, HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }

        List<QuizAttempt> topScores = quizAttemptRepository.findTopScores();
        model.addAttribute("attempts", topScores);
        return "quiz-leaderboard";
    }
} 