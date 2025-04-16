package com.example.bookreview.controller;

import com.example.bookreview.model.User;
import com.example.bookreview.model.CharacterMatchScore;
import com.example.bookreview.service.CharacterMatchService;
import com.example.bookreview.service.UserService;
import com.example.bookreview.repository.CharacterMatchScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/character-match")
public class CharacterMatchController {

    @Autowired
    private CharacterMatchService characterMatchService;

    @Autowired
    private UserService userService;

    @Autowired
    private CharacterMatchScoreRepository scoreRepository;

    @GetMapping("/game")
    public String startGame(Model model, HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(customerName)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        CharacterMatchScore score = characterMatchService.startNewGame(user);
        
        model.addAttribute("score", score);
        model.addAttribute("scrambledTitle", characterMatchService.scrambleTitle(score.getBookTitle()));
        return "character-match";
    }

    @PostMapping("/check")
    @ResponseBody
    public Map<String, Object> checkAnswer(@RequestParam String answer, @RequestParam Long scoreId) {
        Map<String, Object> response = new HashMap<>();
        try {
            CharacterMatchScore score = scoreRepository.findById(scoreId)
                .orElseThrow(() -> new RuntimeException("Score not found"));
            
            score.setAttempts(score.getAttempts() + 1);
            
            if (answer.trim().equalsIgnoreCase(score.getBookTitle().trim())) {
                int points = characterMatchService.calculatePoints(score.getAttempts());
                score.setScore(points);
                scoreRepository.save(score);
                
                response.put("correct", true);
                response.put("message", "Correct! You solved it in " + score.getAttempts() + " attempts!");
                response.put("points", points);
                response.put("attempts", score.getAttempts());
            } else {
                scoreRepository.save(score);
                response.put("correct", false);
                response.put("message", "Incorrect. Try again!");
                response.put("attempts", score.getAttempts());
            }
        } catch (Exception e) {
            response.put("correct", false);
            response.put("message", "An error occurred: " + e.getMessage());
            response.put("attempts", 0);
        }
        return response;
    }

    @GetMapping("/leaderboard")
    public String showLeaderboard(Model model, HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(customerName)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Get user's stats
        Integer totalScore = characterMatchService.getTotalScore(user);
        Integer bestScore = characterMatchService.getBestScore(user);
        Long matchesPlayed = characterMatchService.getMatchesPlayed(user);

        // Get leaderboard
        List<Object[]> leaderboard = characterMatchService.getLeaderboard();

        // Add all attributes to model
        model.addAttribute("customerName", customerName);
        model.addAttribute("totalScore", totalScore != null ? totalScore : 0);
        model.addAttribute("bestScore", bestScore != null ? bestScore : 0);
        model.addAttribute("matchesPlayed", matchesPlayed != null ? matchesPlayed : 0);
        model.addAttribute("leaderboard", leaderboard);

        return "character-match-leaderboard";
    }
} 