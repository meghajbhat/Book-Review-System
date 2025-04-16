package com.example.bookreview.controller;

import com.example.bookreview.model.Book;
import com.example.bookreview.model.Review;
import com.example.bookreview.model.User;
import com.example.bookreview.model.Friendship;
import com.example.bookreview.service.BookService;
import com.example.bookreview.service.ReviewService;
import com.example.bookreview.service.UserService;
import com.example.bookreview.service.FriendshipService;
import com.example.bookreview.repository.QuizAttemptRepository;
import com.example.bookreview.repository.DetectiveCaseRepository;
import com.example.bookreview.model.DetectiveCase;
import com.example.bookreview.repository.BingoCardRepository;
import com.example.bookreview.model.BingoCard;
import com.example.bookreview.repository.CharacterMatchScoreRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.example.bookreview.model.Role;
import com.example.bookreview.service.MessageService;

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

    @Autowired
    private CharacterMatchScoreRepository characterMatchScoreRepository;

    @Autowired
    private FriendshipService friendshipService;

    @Autowired
    private MessageService messageService;

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
    public String showSocialPage(HttpSession session, Model model, @RequestParam(required = false) String search) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }

        User currentUser = userService.findByUsername(customerName).orElse(null);
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<User> users;
        if (search != null && !search.trim().isEmpty()) {
            users = userService.searchUsers(search).stream()
                    .filter(user -> !user.equals(currentUser))
                    .collect(Collectors.toList());
        } else {
            users = userService.getAllUsers().stream()
                    .filter(user -> !user.getRole().equals(Role.ADMIN) && !user.equals(currentUser))
                    .collect(Collectors.toList());
        }

        List<Friendship> pendingRequests = friendshipService.getPendingFriendRequests(currentUser);
        List<Friendship> friends = friendshipService.getFriends(currentUser);
        long unreadMessageCount = messageService.getUnreadMessageCount(currentUser);

        model.addAttribute("users", users);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("friends", friends);
        model.addAttribute("unreadMessageCount", unreadMessageCount);

        return "social";
    }

    @GetMapping("/user/{userId}")
    public String viewUserProfile(@PathVariable Long userId, Model model, HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }

        User currentUser = userService.findByUsername(customerName).orElse(null);
        if (currentUser == null) {
            return "redirect:/login";
        }

        User viewedUser = userService.getUserById(userId);
        if (viewedUser == null) {
            return "redirect:/social";
        }

        List<Review> userReviews = reviewService.getReviewsByUser(viewedUser);
        double averageRating = userReviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);

        Friendship friendship = friendshipService.getFriendshipBetweenUsers(currentUser, viewedUser);
        
        model.addAttribute("viewedUser", viewedUser);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("userReviews", userReviews);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("friendship", friendship);
        
        return "user-profile";
    }

    @PostMapping("/send-friend-request/{userId}")
    public String sendFriendRequest(@PathVariable Long userId, HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }

        User currentUser = userService.findByUsername(customerName).orElse(null);
        User targetUser = userService.getUserById(userId);

        if (currentUser != null && targetUser != null) {
            friendshipService.sendFriendRequest(currentUser, targetUser);
        }

        return "redirect:/social";
    }

    @PostMapping("/accept-friend-request/{friendshipId}")
    public String acceptFriendRequest(@PathVariable Long friendshipId, HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }

        User currentUser = userService.findByUsername(customerName).orElse(null);
        if (currentUser != null) {
            Friendship friendship = friendshipService.getFriendshipById(friendshipId);
            if (friendship != null) {
                friendshipService.acceptFriendRequest(friendship);
            }
        }

        return "redirect:/social";
    }

    @PostMapping("/reject-friend-request/{friendshipId}")
    public String rejectFriendRequest(@PathVariable Long friendshipId, HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }

        User currentUser = userService.findByUsername(customerName).orElse(null);
        if (currentUser != null) {
            Friendship friendship = friendshipService.getFriendshipById(friendshipId);
            if (friendship != null) {
                friendshipService.rejectFriendRequest(friendship);
            }
        }

        return "redirect:/social";
    }

    @PostMapping("/cancel-friend-request/{userId}")
    public String cancelFriendRequest(@PathVariable Long userId, HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }

        User currentUser = userService.findByUsername(customerName).orElse(null);
        User targetUser = userService.getUserById(userId);

        if (currentUser != null && targetUser != null) {
            // Find and delete the pending friend request
            List<Friendship> pendingRequests = friendshipService.getPendingFriendRequests(targetUser);
            pendingRequests.stream()
                .filter(fr -> fr.getRequester().getUserId().equals(currentUser.getUserId()))
                .findFirst()
                .ifPresent(fr -> friendshipService.rejectFriendRequest(fr));
        }

        return "redirect:/social";
    }

    @GetMapping("/about")
    public String aboutPage(Model model, HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }
        model.addAttribute("customerName", customerName);
        return "about";
    }

    @GetMapping("/faqs")
    public String faqsPage(Model model, HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }
        model.addAttribute("customerName", customerName);
        return "faqs";
    }

    @GetMapping("/home")
    public String homePage(Model model, HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }
        return "redirect:/customer-dashboard";
    }

    @GetMapping("/friend-requests")
    public String friendRequestsPage(Model model, HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }

        User currentUser = userService.findByUsername(customerName).orElse(null);
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Only get received friend requests
        List<Friendship> pendingRequests = friendshipService.getReceivedFriendRequests(currentUser);
        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("currentUser", currentUser);
        return "friend-requests";
    }

    @GetMapping("/friends")
    public String friendsPage(Model model, HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }

        User currentUser = userService.findByUsername(customerName).orElse(null);
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<Friendship> friends = friendshipService.getFriends(currentUser);
        model.addAttribute("friends", friends);
        model.addAttribute("currentUser", currentUser);
        return "friends";
    }

    @PostMapping("/remove-friend/{userId}")
    public String removeFriend(@PathVariable Long userId, HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }

        User currentUser = userService.findByUsername(customerName).orElse(null);
        User friendUser = userService.getUserById(userId);

        if (currentUser != null && friendUser != null) {
            friendshipService.removeFriend(currentUser, friendUser);
        }

        return "redirect:/friends";
    }

    @GetMapping("/messages")
    public String messagesPage(Model model, HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }

        User currentUser = userService.findByUsername(customerName).orElse(null);
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Get all conversations for the current user
        List<Map<String, Object>> conversations = messageService.getConversations(currentUser);
        long unreadMessageCount = messageService.getUnreadMessageCount(currentUser);
        List<Friendship> pendingRequests = friendshipService.getPendingFriendRequests(currentUser);

        model.addAttribute("conversations", conversations);
        model.addAttribute("unreadMessageCount", unreadMessageCount);
        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("currentUser", currentUser);

        return "messages";
    }
}
