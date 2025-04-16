package com.example.bookreview.controller;

import com.example.bookreview.model.User;
import com.example.bookreview.model.DetectiveCase;
import com.example.bookreview.service.DetectiveCaseService;
import com.example.bookreview.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.List;

@Controller
@RequestMapping("/detective")
public class DetectiveController {

    @Autowired
    private DetectiveCaseService detectiveCaseService;

    @Autowired
    private UserService userService;

    @GetMapping("/case")
    public String newCase(Model model, HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(customerName)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        DetectiveCase detectiveCase = detectiveCaseService.createNewCase(user);
        
        model.addAttribute("case", detectiveCase);
        return "detective-case";
    }

    @PostMapping("/solve/{caseId}")
    @ResponseBody
    public String solveCase(@PathVariable Long caseId, @RequestParam String answer) {
        return detectiveCaseService.checkClue(answer, caseId);
    }

    @GetMapping("/case-history")
    public String showCaseHistory(Model model, HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(customerName)
            .orElseThrow(() -> new RuntimeException("User not found"));

        List<DetectiveCase> cases = detectiveCaseService.getUserCases(user);
        int solvedCases = (int) cases.stream().filter(DetectiveCase::isSolved).count();
        int totalPoints = cases.stream().mapToInt(DetectiveCase::getPointsEarned).sum();

        model.addAttribute("cases", cases);
        model.addAttribute("solvedCases", solvedCases);
        model.addAttribute("totalPoints", totalPoints);
        
        return "detective-history";
    }

    @GetMapping("/leaderboard")
    public String showLeaderboard(Model model) {
        List<Object[]> leaderboard = detectiveCaseService.getLeaderboard();
        model.addAttribute("leaderboard", leaderboard);
        return "detective-leaderboard";
    }
} 