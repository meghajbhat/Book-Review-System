package com.example.bookreview.controller;

import com.example.bookreview.model.Book;
import com.example.bookreview.model.Review;
import com.example.bookreview.model.User;
import com.example.bookreview.service.BookService;
import com.example.bookreview.service.ReviewService;
import com.example.bookreview.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    // ✅ REST API to fetch all reviews (if needed)
    @ResponseBody
    @GetMapping
    public List<Review> getAllReviews() {
        return reviewService.getAllReviews();
    }

    // ✅ Submit a new review
    @PostMapping("/submit")
    public void addReview(
            @RequestParam Long bookId,
            @RequestParam String username,
            @RequestParam int rating,
            @RequestParam String comment,
            @RequestParam(value = "anonymous", required = false) String anonymous,
            HttpServletResponse response) throws IOException {

        Book book = bookService.getBookById(bookId);
        User user = userService.findByUsername(username).orElse(null);

        if (book == null || user == null) {
            response.sendRedirect("/books/" + bookId + "?error=" +
                    URLEncoder.encode("Invalid review submission 😕", "UTF-8"));
            return;
        }

        Optional<Review> existingReview = reviewService.findByUserAndBook(user, book);
        if (existingReview.isPresent()) {
            response.sendRedirect("/books/" + bookId + "?error=" +
                    URLEncoder.encode("Oops! You already reviewed this book 😏 Only one review allowed.", "UTF-8"));
            return;
        }

        Review review = new Review();
        review.setBook(book);
        review.setUser(user);
        review.setRating(rating);
        review.setComment((anonymous != null) ? "[Anonymous] " + comment : comment);

        reviewService.addReview(review);
        response.sendRedirect("/books/" + bookId + "?success=" +
                URLEncoder.encode("Review submitted successfully! ✅", "UTF-8"));
    }

    // ✅ Redirect GET requests to /submit to avoid blank page
    @GetMapping("/submit")
    public void redirectFromGet(HttpServletResponse response) throws IOException {
        response.sendRedirect("/customer-dashboard");
    }

    // ✅ Delete review (allowed for admin or same user)
    @PostMapping("/delete")
    public String deleteReview(
            @RequestParam Long reviewId,
            @RequestParam Long bookId,
            HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        String adminName = (String) session.getAttribute("adminName");
        boolean isAdmin = (adminName != null);

        Review review = reviewService.getReviewById(reviewId);
        if (review == null) {
            return "redirect:/books/" + bookId + "?error=ReviewNotFound";
        }

        if (isAdmin || (customerName != null && review.getUser().getUsername().equals(customerName))) {
            reviewService.deleteReviewById(reviewId);
            return "redirect:/books/" + bookId + "?success=" +
                    URLEncoder.encode("Review deleted ✅", java.nio.charset.StandardCharsets.UTF_8);
        }

        return "redirect:/books/" + bookId + "?error=" +
                URLEncoder.encode("You are not authorized to delete this review.",
                        java.nio.charset.StandardCharsets.UTF_8);
    }

    // ✅ Prevent GET access to /delete/{id} from blanking
    @GetMapping("/delete/{id}")
    public String preventDirectGetDelete() {
        return "redirect:/customer-dashboard";
    }
}
