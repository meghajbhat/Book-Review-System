package com.example.bookreview.controller;

import com.example.bookreview.model.Book;
import com.example.bookreview.model.Review;
import com.example.bookreview.service.BookService;
import com.example.bookreview.service.ReviewService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private ReviewService reviewService;

    @GetMapping
    public String getAllBooks(Model model) {
        List<Book> books = bookService.getAllBooks();
        model.addAttribute("books", books);
        return "customer-dashboard";
    }

    @PostMapping("/add")
    public String addBook(@RequestParam String title, @RequestParam String author, @RequestParam Double price) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setPrice(price);
        bookService.addBook(book);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/{id}")
    public String showBookDetails(
            @PathVariable Long id,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "filter", required = false) String filter,
            Model model,
            HttpSession session) {

        Book book = bookService.getBookById(id);
        if (book == null) {
            return "redirect:/customer-dashboard?error=Book not found.";
        }

        String customerName = (String) session.getAttribute("customerName");

        // Get and sort reviews
        List<Review> reviews = reviewService.getSortedAndFilteredReviews(book, sort, filter);
        Double avgRating = reviewService.getAverageRatingForBook(book);

        model.addAttribute("book", book);
        model.addAttribute("reviews", reviews);
        model.addAttribute("avgRating", avgRating);
        model.addAttribute("customerName", customerName);
        model.addAttribute("sort", sort);
        model.addAttribute("filter", filter);
        return "book-details";
    }

    @GetMapping("/search")
    public String searchBooks(@RequestParam String query, Model model) {
        List<Book> books = bookService.searchBooks(query);
        model.addAttribute("allBooks", books);
        model.addAttribute("searchQuery", query);
        return "customer-dashboard";
    }

}
