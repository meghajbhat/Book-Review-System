package com.example.bookreview.controller;

import com.example.bookreview.model.Book;
import com.example.bookreview.model.Review;
import com.example.bookreview.model.User;
import com.example.bookreview.service.BookService;
import com.example.bookreview.service.ReviewService;
import com.example.bookreview.service.UserService;
import com.example.bookreview.repository.QuizAttemptRepository;
import com.example.bookreview.repository.DetectiveCaseRepository;
import com.example.bookreview.model.DetectiveCase;
import com.example.bookreview.repository.BingoCardRepository;
import com.example.bookreview.model.BingoCard;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class CustomerController {

    @Autowired
    private BookService bookService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private DetectiveCaseRepository detectiveCaseRepository;

    @Autowired
    private BingoCardRepository bingoCardRepository;

    @GetMapping("/customer-dashboard")
    public String showCustomerDashboard(
            HttpSession session,
            Model model,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "success", required = false) String success) {

        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }

        // Get user's reviews
        User user = userService.findByUsername(customerName).orElse(null);
        if (user != null) {
            List<Review> userReviews = reviewService.getReviewsByUser(user);
            model.addAttribute("userReviews", userReviews);
        }

        // Get all books
        List<Book> allBooks = bookService.getAllBooks();

        // Set average ratings and create review counts map
        Map<Long, Long> reviewCounts = new HashMap<>();
        for (Book book : allBooks) {
            Double avgRating = reviewService.getAverageRatingForBook(book);
            if (avgRating != null) {
                book.setRating(avgRating);
            }
            reviewCounts.put(book.getId(), reviewService.getReviewCountForBook(book));
        }

        // Get top 5 trending books (by rating)
        List<Book> trendingBooks = allBooks.stream()
                .filter(b -> b.getRating() != null)
                .sorted((b1, b2) -> b2.getRating().compareTo(b1.getRating()))
                .limit(5)
                .collect(Collectors.toList());

        // Get recently added books (last 10)
        List<Book> recentBooks = allBooks.stream()
                .sorted((b1, b2) -> b2.getId().compareTo(b1.getId()))
                .limit(10)
                .collect(Collectors.toList());

        // Add attributes to model
        model.addAttribute("customerName", customerName);
        model.addAttribute("allBooks", allBooks);
        model.addAttribute("trendingBooks", trendingBooks);
        model.addAttribute("recentBooks", recentBooks);
        model.addAttribute("reviewCounts", reviewCounts);

        if (error != null)
            model.addAttribute("error", error);
        if (success != null)
            model.addAttribute("success", success);

        return "customer-dashboard";
    }

    @GetMapping("/library")
    public String libraryPage(Model model, HttpSession session) {
        // Get customer name from session
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }

        List<Book> allBooks = bookService.getAllBooks();

        // Set average ratings for all books
        for (Book book : allBooks) {
            Double avgRating = reviewService.getAverageRatingForBook(book);
            Long reviewCount = reviewService.getReviewCountForBook(book);
            if (avgRating != null) {
                book.setRating(avgRating);
            }
            model.addAttribute("reviewCount_" + book.getId(), reviewCount);
        }

        // Create maps for each genre category
        Map<String, List<Book>> romanceBooks = new LinkedHashMap<>();
        Map<String, List<Book>> historicalBooks = new LinkedHashMap<>();
        Map<String, List<Book>> scifiBooks = new LinkedHashMap<>();
        Map<String, List<Book>> thrillerBooks = new LinkedHashMap<>();
        Map<String, List<Book>> fantasyBooks = new LinkedHashMap<>();
        Map<String, List<Book>> adventureBooks = new LinkedHashMap<>();
        Map<String, List<Book>> mysteryBooks = new LinkedHashMap<>();
        Map<String, List<Book>> horrorBooks = new LinkedHashMap<>();

        // Create a map to store review counts
        Map<Long, Long> reviewCounts = new HashMap<>();
        for (Book book : allBooks) {
            reviewCounts.put(book.getId(), reviewService.getReviewCountForBook(book));
        }
        model.addAttribute("reviewCounts", reviewCounts);

        // Categorize books by individual genres
        for (Book book : allBooks) {
            String genres = book.getGenre();
            if (genres != null && !genres.isEmpty()) {
                if (genres.toLowerCase().contains("romance")) {
                    romanceBooks.computeIfAbsent("Romance", k -> new ArrayList<>()).add(book);
                }
                if (genres.toLowerCase().contains("historical")) {
                    historicalBooks.computeIfAbsent("Historical Fiction", k -> new ArrayList<>()).add(book);
                }
                if (genres.toLowerCase().contains("sci-fi") || genres.toLowerCase().contains("sci fi")) {
                    scifiBooks.computeIfAbsent("Science Fiction", k -> new ArrayList<>()).add(book);
                }
                if (genres.toLowerCase().contains("thriller")) {
                    thrillerBooks.computeIfAbsent("Thriller", k -> new ArrayList<>()).add(book);
                }
                if (genres.toLowerCase().contains("fantasy")) {
                    fantasyBooks.computeIfAbsent("Fantasy", k -> new ArrayList<>()).add(book);
                }
                if (genres.toLowerCase().contains("adventure")) {
                    adventureBooks.computeIfAbsent("Adventure", k -> new ArrayList<>()).add(book);
                }
                if (genres.toLowerCase().contains("mystery")) {
                    mysteryBooks.computeIfAbsent("Mystery", k -> new ArrayList<>()).add(book);
                }
                if (genres.toLowerCase().contains("horror")) {
                    horrorBooks.computeIfAbsent("Horror", k -> new ArrayList<>()).add(book);
                }
            }
        }

        // Add non-empty genre maps to the model
        if (!romanceBooks.isEmpty()) model.addAttribute("romanceBooks", romanceBooks);
        if (!historicalBooks.isEmpty()) model.addAttribute("historicalBooks", historicalBooks);
        if (!scifiBooks.isEmpty()) model.addAttribute("scifiBooks", scifiBooks);
        if (!thrillerBooks.isEmpty()) model.addAttribute("thrillerBooks", thrillerBooks);
        if (!fantasyBooks.isEmpty()) model.addAttribute("fantasyBooks", fantasyBooks);
        if (!adventureBooks.isEmpty()) model.addAttribute("adventureBooks", adventureBooks);
        if (!mysteryBooks.isEmpty()) model.addAttribute("mysteryBooks", mysteryBooks);
        if (!horrorBooks.isEmpty()) model.addAttribute("horrorBooks", horrorBooks);

        // Add customer name to the model
        model.addAttribute("customerName", customerName);

        return "library";
    }


    @GetMapping("/social")
    public String socialPage() {
        return "social";
    }

    @GetMapping("/about")
    public String aboutPage() {
        return "about";
    }

    @GetMapping("/faqs")
    public String faqsPage() {
        return "faqs";
    }

    @GetMapping("/challenges")
    public String challengesPage(Model model, HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.findByUsername(customerName);
        if (!userOpt.isPresent()) {
            return "redirect:/login";
        }

        User user = userOpt.get();
        
        // Quiz statistics
        boolean hasAttemptedQuiz = quizAttemptRepository.hasAttemptedQuiz(user);
        long attemptCount = quizAttemptRepository.getAttemptCount(user);
        Integer bestScore = quizAttemptRepository.getBestScore(user);

        // Detective statistics
        long solvedCases = detectiveCaseRepository.countSolvedCases(user);
        List<DetectiveCase> allCases = detectiveCaseRepository.findByUserOrderByStartTimeDesc(user);
        int totalAttempts = allCases.stream().mapToInt(DetectiveCase::getAttemptsUsed).sum();
        int totalPoints = detectiveCaseRepository.getTotalPoints(user) != null ? detectiveCaseRepository.getTotalPoints(user) : 0;
        
        // Calculate accuracy
        double accuracy = allCases.isEmpty() ? 0 : (solvedCases * 100.0) / allCases.size();

        // Bingo statistics
        long bingoCardsWon = bingoCardRepository.countCompletedCards(user);
        Optional<BingoCard> currentCard = bingoCardRepository.findFirstByUserAndCompletedFalseOrderByCreatedAtDesc(user);
        Double currentBingoProgress = currentCard.map(BingoCard::getProgress).orElse(0.0);
        int bingoChallengesCompleted = currentCard.map(card -> card.getCompletedChallenges().size()).orElse(0);

        // Quiz attributes
        model.addAttribute("hasAttemptedQuiz", hasAttemptedQuiz);
        model.addAttribute("quizAttemptCount", attemptCount);
        model.addAttribute("quizBestScore", bestScore != null ? bestScore : 0);
        
        // Detective attributes
        model.addAttribute("detectiveSolvedCases", solvedCases);
        model.addAttribute("detectiveAccuracy", Math.round(accuracy));
        model.addAttribute("detectivePoints", totalPoints);
        model.addAttribute("detectiveAttempts", totalAttempts);

        // Bingo attributes
        model.addAttribute("bingoCardsWon", bingoCardsWon);
        model.addAttribute("currentBingoProgress", Math.round(currentBingoProgress));
        model.addAttribute("bingoChallengesCompleted", bingoChallengesCompleted);
        
        model.addAttribute("customerName", customerName);
        return "challenges";
    }

}
