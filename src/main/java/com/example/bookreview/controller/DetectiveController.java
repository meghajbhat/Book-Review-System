package com.example.bookreview.controller;

import com.example.bookreview.model.Book;
import com.example.bookreview.model.DetectiveCase;
import com.example.bookreview.model.User;
import com.example.bookreview.repository.DetectiveCaseRepository;
import com.example.bookreview.service.BookService;
import com.example.bookreview.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Controller
@RequestMapping("/detective")
public class DetectiveController {

    @Autowired
    private DetectiveCaseRepository detectiveCaseRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @GetMapping("/new-case")
    public String newCase(Model model, HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.findByUsername(customerName);
        if (!userOpt.isPresent()) {
            return "redirect:/login";
        }

        // Get a random book for the case
        List<Book> books = bookService.getAllBooks();
        if (books.isEmpty()) {
            return "redirect:/challenges?error=No books available for cases";
        }

        Random random = new Random();
        Book selectedBook = books.get(random.nextInt(books.size()));

        // Generate clues based on the selected book
        String clue1 = "Title Length: The book's title has " + selectedBook.getTitle().length() + " characters.";
        String clue2 = "Author Hint: The author's name starts with '" + selectedBook.getAuthor().charAt(0) + "'.";
        String clue3 = "Genre: The book belongs to the " + selectedBook.getGenre() + " genre.";

        // Create a new case
        DetectiveCase newCase = new DetectiveCase(
            userOpt.get(),
            selectedBook,
            "Case #" + System.currentTimeMillis() % 10000,
            clue1,
            clue2,
            clue3,
            selectedBook.getTitle()
        );
        detectiveCaseRepository.save(newCase);

        model.addAttribute("case", newCase);
        model.addAttribute("customerName", customerName);
        return "detective-case";
    }

    @GetMapping("/case-history")
    public String caseHistory(Model model, HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.findByUsername(customerName);
        if (!userOpt.isPresent()) {
            return "redirect:/login";
        }

        User user = userOpt.get();
        List<DetectiveCase> cases = detectiveCaseRepository.findByUserOrderByStartTimeDesc(user);
        long solvedCases = detectiveCaseRepository.countSolvedCases(user);
        Integer totalPoints = detectiveCaseRepository.getTotalPoints(user);

        model.addAttribute("cases", cases);
        model.addAttribute("solvedCases", solvedCases);
        model.addAttribute("totalPoints", totalPoints != null ? totalPoints : 0);
        model.addAttribute("customerName", customerName);
        return "detective-history";
    }

    @PostMapping("/solve/{caseId}")
    @ResponseBody
    public String solvePuzzle(@PathVariable Long caseId, @RequestParam String answer, HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "Please log in to solve cases";
        }

        Optional<DetectiveCase> caseOpt = detectiveCaseRepository.findById(caseId);
        if (!caseOpt.isPresent()) {
            return "Case not found";
        }

        DetectiveCase detectiveCase = caseOpt.get();
        if (detectiveCase.isSolved()) {
            return "This case has already been solved";
        }

        detectiveCase.setAttemptsUsed(detectiveCase.getAttemptsUsed() + 1);

        if (answer.trim().equalsIgnoreCase(detectiveCase.getSolution().trim())) {
            detectiveCase.setSolved(true);
            // Points calculation: 100 base points - 10 points per attempt used (minimum 20 points)
            int points = Math.max(20, 100 - (detectiveCase.getAttemptsUsed() - 1) * 10);
            detectiveCase.setPointsEarned(points);
            detectiveCaseRepository.save(detectiveCase);
            return "Correct! You've earned " + points + " points!";
        } else {
            detectiveCaseRepository.save(detectiveCase);
            return "Incorrect. Try another clue!";
        }
    }

    @GetMapping("/leaderboard")
    public String showLeaderboard(Model model, HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }

        // Get top detectives by points
        List<Object[]> topDetectives = detectiveCaseRepository.findTopDetectivesByPoints();
        List<DetectiveStats> detectives = new ArrayList<>();

        for (Object[] result : topDetectives) {
            DetectiveStats stats = new DetectiveStats();
            stats.setUsername((String) result[0]);
            stats.setPoints(((Number) result[1]).intValue());
            stats.setSolvedCases(((Number) result[2]).intValue());
            detectives.add(stats);
        }

        model.addAttribute("detectives", detectives);
        model.addAttribute("customerName", customerName);
        return "detective-leaderboard";
    }

    // Helper class for leaderboard stats
    @Getter
    @Setter
    private static class DetectiveStats {
        private String username;
        private int points;
        private int solvedCases;
    }
} 