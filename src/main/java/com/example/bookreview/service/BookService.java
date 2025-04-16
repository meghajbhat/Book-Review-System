package com.example.bookreview.service;

import com.example.bookreview.model.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.bookreview.repository.BookRepository;
import com.example.bookreview.repository.DetectiveCaseRepository;
import com.example.bookreview.model.DetectiveCase;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private DetectiveCaseRepository detectiveCaseRepository;

    public void initializeSampleBooks() {
        if (bookRepository.count() == 0) {
            List<Book> sampleBooks = new ArrayList<>();
            
            // Add some sample books
            Book book1 = new Book();
            book1.setTitle("The Great Gatsby");
            book1.setAuthor("F. Scott Fitzgerald");
            book1.setGenre("Classic, Fiction");
            book1.setSummary("A story of the fabulously wealthy Jay Gatsby and his love for the beautiful Daisy Buchanan.");
            book1.setPrice(12.99);
            sampleBooks.add(book1);

            Book book2 = new Book();
            book2.setTitle("To Kill a Mockingbird");
            book2.setAuthor("Harper Lee");
            book2.setGenre("Classic, Fiction");
            book2.setSummary("The story of racial injustice and the loss of innocence in the American South.");
            book2.setPrice(14.99);
            sampleBooks.add(book2);

            Book book3 = new Book();
            book3.setTitle("1984");
            book3.setAuthor("George Orwell");
            book3.setGenre("Science Fiction, Dystopian");
            book3.setSummary("A dystopian social science fiction novel and cautionary tale.");
            book3.setPrice(11.99);
            sampleBooks.add(book3);

            Book book4 = new Book();
            book4.setTitle("Pride and Prejudice");
            book4.setAuthor("Jane Austen");
            book4.setGenre("Romance, Classic");
            book4.setSummary("A romantic novel of manners that follows the emotional development of Elizabeth Bennet.");
            book4.setPrice(10.99);
            sampleBooks.add(book4);

            Book book5 = new Book();
            book5.setTitle("The Hobbit");
            book5.setAuthor("J.R.R. Tolkien");
            book5.setGenre("Fantasy, Adventure");
            book5.setSummary("The adventure of Bilbo Baggins, a hobbit who embarks on a quest to help a group of dwarves.");
            book5.setPrice(13.99);
            sampleBooks.add(book5);

            bookRepository.saveAll(sampleBooks);
        }
    }

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

    public void deleteBook(Long id) {
        // First delete all related detective cases
        List<DetectiveCase> cases = detectiveCaseRepository.findByBookId(id);
        detectiveCaseRepository.deleteAll(cases);
        
        // Then delete the book
        bookRepository.deleteById(id);
    }

}
