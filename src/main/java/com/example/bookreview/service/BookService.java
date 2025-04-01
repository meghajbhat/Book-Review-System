package com.example.bookreview.service;

import com.example.bookreview.model.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.bookreview.repository.BookRepository;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public void addBook(Book book) {
        bookRepository.save(book);
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id).orElse(null);
    }

    public void updateBook(Book book) {
        bookRepository.save(book); // save() will update if ID already exists
    }

    public List<Book> searchByTitle(String keyword) {
        return bookRepository.findByTitleContainingIgnoreCase(keyword);
    }

    public List<Book> searchByAuthor(String keyword) {
        return bookRepository.findByAuthorContainingIgnoreCase(keyword);
    }

    public List<Book> searchByGenre(String keyword) {
        return bookRepository.findByGenreContainingIgnoreCase(keyword);
    }

    public List<Book> searchBooksByTitle(String keyword) {
        return bookRepository.findByTitleContainingIgnoreCase(keyword);
    }

    public List<Book> searchBooksByAuthor(String keyword) {
        return bookRepository.findByAuthorContainingIgnoreCase(keyword);
    }

    public List<Book> searchBooksByGenre(String keyword) {
        return bookRepository.findByGenreContainingIgnoreCase(keyword);
    }

    public List<Book> getTop5SpotlightBooks() {
        return bookRepository.findTop5ByOrderByRatingDescCreatedAtDesc(); // Or adjust logic as needed
    }

    public List<Book> searchBooks(String keyword) {
        List<Book> books = new ArrayList<>();
        books.addAll(searchBooksByTitle(keyword));
        books.addAll(searchBooksByAuthor(keyword));
        books.addAll(searchBooksByGenre(keyword));
        return books.stream().distinct().collect(Collectors.toList());
    }

}
