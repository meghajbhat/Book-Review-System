package com.example.bookreview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.bookreview.model.Book;
import com.example.bookreview.model.Review;
import com.example.bookreview.model.User;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByUserAndBook(User user, Book book);

    List<Review> findByBook(Book book);

    // ✅ Average rating per book
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book = :book")
    Double findAverageRatingByBook(@Param("book") Book book);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.book = :book")
    Long countByBook(@Param("book") Book book);

    void deleteById(Long id);

    Optional<Review> findById(Long id);

    List<Review> findByUser(User user);

}
