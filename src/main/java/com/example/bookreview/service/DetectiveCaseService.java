package com.example.bookreview.service;

import com.example.bookreview.model.DetectiveCase;
import com.example.bookreview.model.User;
import com.example.bookreview.model.Book;
import com.example.bookreview.repository.DetectiveCaseRepository;
import com.example.bookreview.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class DetectiveCaseService {

    @Autowired
    private DetectiveCaseRepository detectiveCaseRepository;

    @Autowired
    private BookRepository bookRepository;

    public DetectiveCase createNewCase(User user) {
        // Get a random book
        List<Book> books = bookRepository.findAll();
        if (books.isEmpty()) {
            throw new RuntimeException("No books available for cases");
        }

        Random random = new Random();
        Book selectedBook = books.get(random.nextInt(books.size()));

        // Generate clues based on the selected book
        String clue1 = "Title Length: The book's title has " + selectedBook.getTitle().length() + " characters.";
        String clue2 = "Author Hint: The author's name starts with '" + selectedBook.getAuthor().charAt(0) + "'.";
        String clue3 = "Genre: The book belongs to the " + selectedBook.getGenre() + " genre.";

        // Create a new case
        DetectiveCase newCase = new DetectiveCase();
        newCase.setUser(user);
        newCase.setBook(selectedBook);
        newCase.setCaseName("Case #" + System.currentTimeMillis() % 10000);
        newCase.setClue1(clue1);
        newCase.setClue2(clue2);
        newCase.setClue3(clue3);
        newCase.setSolution(selectedBook.getTitle());
        newCase.setStartTime(LocalDateTime.now());
        newCase.setSolved(false);
        newCase.setAttemptsUsed(0);
        newCase.setPointsEarned(0);

        return detectiveCaseRepository.save(newCase);
    }

    public String checkClue(String answer, Long caseId) {
        DetectiveCase detectiveCase = detectiveCaseRepository.findById(caseId)
            .orElseThrow(() -> new RuntimeException("Case not found"));

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

    public List<Object[]> getLeaderboard() {
        return detectiveCaseRepository.findTopDetectivesByPoints();
    }

    public List<DetectiveCase> getUserCases(User user) {
        return detectiveCaseRepository.findByUserOrderByStartTimeDesc(user);
    }
} 