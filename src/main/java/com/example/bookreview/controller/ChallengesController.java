package com.example.bookreview.controller;

import com.example.bookreview.model.*;
import com.example.bookreview.repository.*;
import com.example.bookreview.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class ChallengesController {

    @Autowired
    private UserService userService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private FriendshipService friendshipService;
    @Autowired
    private QuizAttemptRepository quizAttemptRepository;
    @Autowired
    private DetectiveCaseRepository detectiveCaseRepository;
    @Autowired
    private BingoCardRepository bingoCardRepository;
    @Autowired
    private CharacterMatchScoreRepository characterMatchScoreRepository;
    @Autowired
    private DetectiveCaseService detectiveCaseService;
    @Autowired
    private CharacterMatchService characterMatchService;

    @GetMapping("/challenges")
    public String showChallengesPage(Model model, HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(customerName).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        // Review and Friend Challenges
        List<Review> userReviews = reviewService.findByUser(user);
        List<Friendship> acceptedFriendships = friendshipService.getFriends(user);
        int reviewCount = userReviews.size();
        int friendCount = acceptedFriendships.size();
        int reviewTarget = 5;
        int friendTarget = 5;
        double reviewProgress = (double) reviewCount / reviewTarget * 100;
        double friendProgress = (double) friendCount / friendTarget * 100;

        // Quiz stats
        long quizAttemptCount = quizAttemptRepository.getAttemptCount(user);
        Integer quizBestScore = quizAttemptRepository.getBestScore(user);
        boolean hasAttemptedQuiz = quizAttemptRepository.hasAttemptedQuiz(user);

        // Detective stats
        List<DetectiveCase> detectiveCases = detectiveCaseRepository.findByUserOrderByStartTimeDesc(user);
        int detectiveAttempts = detectiveCases.size();
        int detectiveSolvedCases = (int) detectiveCaseRepository.countSolvedCases(user);
        double detectiveAccuracy = detectiveAttempts > 0 ? 
                (double) detectiveSolvedCases / detectiveAttempts * 100 : 0;
        int detectivePoints = detectiveCaseRepository.getTotalPoints(user);

        // Bingo stats
        List<BingoCard> bingoCards = bingoCardRepository.findByUserOrderByCreatedAtDesc(user);
        int bingoCardsWon = (int) bingoCardRepository.countCompletedCards(user);
        int currentBingoProgress = bingoCards.stream()
                .filter(card -> !card.isCompleted())
                .findFirst()
                .map(card -> (int) card.getProgress())
                .orElse(0);
        int bingoChallengesCompleted = bingoCards.stream()
                .mapToInt(card -> card.getCompletedChallenges().size())
                .sum();

        // Character Match stats
        int characterMatchesCount = characterMatchScoreRepository.countMatchesPlayed(user).intValue();
        Integer characterBestScore = characterMatchScoreRepository.findBestScore(user);
        int characterLevel = characterMatchesCount / 5 + 1; // Level up every 5 matches

        // Add all attributes to the model
        model.addAttribute("customerName", customerName);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("friendCount", friendCount);
        model.addAttribute("reviewTarget", reviewTarget);
        model.addAttribute("friendTarget", friendTarget);
        model.addAttribute("reviewProgress", reviewProgress);
        model.addAttribute("friendProgress", friendProgress);
        model.addAttribute("quizAttemptCount", quizAttemptCount);
        model.addAttribute("quizBestScore", quizBestScore != null ? quizBestScore : 0);
        model.addAttribute("hasAttemptedQuiz", hasAttemptedQuiz);
        model.addAttribute("detectiveAttempts", detectiveAttempts);
        model.addAttribute("detectiveSolvedCases", detectiveSolvedCases);
        model.addAttribute("detectiveAccuracy", detectiveAccuracy);
        model.addAttribute("detectivePoints", detectivePoints);
        model.addAttribute("bingoCardsWon", bingoCardsWon);
        model.addAttribute("currentBingoProgress", currentBingoProgress);
        model.addAttribute("bingoChallengesCompleted", bingoChallengesCompleted);
        model.addAttribute("characterMatchesCount", characterMatchesCount);
        model.addAttribute("characterBestScore", characterBestScore != null ? characterBestScore : 0);
        model.addAttribute("characterLevel", characterLevel);

        return "challenges";
    }

    @GetMapping("/detective/new-case")
    public String startNewDetectiveCase(HttpSession session, Model model) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(customerName).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        // Create a new detective case
        DetectiveCase newCase = detectiveCaseService.createNewCase(user);
        model.addAttribute("case", newCase);
        return "detective-case";
    }
} 