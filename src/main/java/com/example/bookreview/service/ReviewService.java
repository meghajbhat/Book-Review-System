package com.example.bookreview.service;

import com.example.bookreview.model.Book;
import com.example.bookreview.model.Review;
import com.example.bookreview.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.bookreview.model.User;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.List;

import java.util.Comparator;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    public Review addReview(Review review) {
        return reviewRepository.save(review);
    }

    public Optional<Review> findByUserAndBook(User user, Book book) {
    return reviewRepository.findByUserAndBook(user, book);
    }

    public List<Review> getReviewsForBook(Book book) {
        return reviewRepository.findByBook(book);
    }

    public Double getAverageRatingForBook(Book book) {
        return reviewRepository.findAverageRatingByBook(book);
    }

    public Long getReviewCountForBook(Book book) {
        return reviewRepository.countByBook(book);
    }

    public void deleteReviewById(Long id) {
        reviewRepository.deleteById(id);
    }

    public Review getReviewById(Long id) {
        return reviewRepository.findById(id).orElse(null);
    }

    public List<Review> getSortedAndFilteredReviews(Book book, String sort, String filter) {
        List<Review> reviews = reviewRepository.findByBook(book);

        // Apply filtering
        if (filter != null && !filter.equals("all")) {
            int targetRating = Integer.parseInt(filter);
            reviews = reviews.stream()
                    .filter(r -> r.getRating() == targetRating)
                    .collect(Collectors.toList());
        }

        // Apply sorting
        if (sort != null) {
            switch (sort) {
                case "newest":
                    reviews.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
                    break;
                case "oldest":
                    reviews.sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
                    break;
                case "highest":
                    reviews.sort((a, b) -> Integer.compare(b.getRating(), a.getRating()));
                    break;
                case "lowest":
                    reviews.sort((a, b) -> Integer.compare(a.getRating(), b.getRating()));
                    break;
            }
        } else {
            // Default sort by newest first
            reviews.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        }

        return reviews;
    }

    public List<Review> getReviewsByUser(User user) {
        return reviewRepository.findByUser(user);
    }

    public List<Review> findByUser(User user) {
        return reviewRepository.findByUser(user);
    }

}
