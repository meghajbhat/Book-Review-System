package com.example.bookreview.controller;

import com.example.bookreview.model.Book;
import com.example.bookreview.model.Review;
import com.example.bookreview.model.User;
import com.example.bookreview.service.BookService;
import com.example.bookreview.service.ReviewService;
import com.example.bookreview.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @Autowired
    private ReviewService reviewService;

    private static final String UPLOAD_DIR = "src/main/resources/static/images/";

    // ✅ Admin Dashboard with search support
    @GetMapping("/dashboard")
    public String showAdminDashboard(
            HttpSession session,
            Model model,
            @RequestParam(value = "searchValue", required = false) String searchValue,
            @RequestParam(value = "searchField", required = false) String searchField) {

        String adminName = (String) session.getAttribute("adminName");
        if (adminName == null)
            return "redirect:/login";

        List<Book> books;

        if (searchValue != null && !searchValue.isEmpty() && searchField != null) {
            switch (searchField) {
                case "title":
                    books = bookService.searchBooksByTitle(searchValue);
                    break;
                case "author":
                    books = bookService.searchBooksByAuthor(searchValue);
                    break;
                case "genre":
                    books = bookService.searchBooksByGenre(searchValue);
                    break;
                default:
                    books = bookService.getAllBooks();
            }
        } else {
            books = bookService.getAllBooks();
        }

        model.addAttribute("books", books);
        model.addAttribute("adminName", adminName);
        model.addAttribute("searchValue", searchValue);
        model.addAttribute("searchField", searchField);

        return "admin-dashboard";
    }

    @GetMapping("/manage-users")
    public String manageUsers(@RequestParam(value = "search", required = false) String search,
            HttpSession session, Model model) {
        String adminName = (String) session.getAttribute("adminName");
        if (adminName == null)
            return "redirect:/login";

        List<User> users = (search != null && !search.isEmpty()) ? userService.searchUsers(search)
                : userService.getAllUsers();

        model.addAttribute("users", users);
        model.addAttribute("search", search);
        return "manage-users";
    }

    @PostMapping("/add-book")
    public String addBook(@RequestParam String title,
            @RequestParam String author,
            @RequestParam Double price,
            @RequestParam(name = "genre", required = false) List<String> genres,
            @RequestParam String summary,
            @RequestParam("imageFile") MultipartFile imageFile) {
        String imagePath = null;

        if (!imageFile.isEmpty()) {
            try {
                String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
                Path path = Paths.get(UPLOAD_DIR + fileName);
                Files.copy(imageFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                imagePath = "/images/" + fileName;
            } catch (IOException e) {
                e.printStackTrace();
                return "redirect:/admin/dashboard?error=ImageUploadFailed";
            }
        }

        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setPrice(price);
        book.setImagePath(imagePath);
        book.setSummary(summary); // ✅ Set summary here
        book.setGenre((genres != null && !genres.isEmpty()) ? String.join(", ", genres) : "Unknown");

        bookService.addBook(book);
        return "redirect:/admin/dashboard?success=BookAdded";
    }

    @PostMapping("/disable/{id}")
    public String disableUser(@PathVariable Long id) {
        userService.disableUser(id);
        return "redirect:/admin/manage-users";
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/manage-users";
    }

    @PostMapping("/enable/{id}")
    public String enableUser(@PathVariable Long id) {
        userService.enableUser(id);
        return "redirect:/admin/manage-users";
    }

    @GetMapping("/edit-book/{id}")
    public String showEditBookForm(@PathVariable Long id, Model model, HttpSession session) {
        if (session.getAttribute("adminName") == null)
            return "redirect:/login";
        Book book = bookService.getBookById(id);
        model.addAttribute("book", book);
        return "edit-book";
    }

    @PostMapping("/update-book/{id}")
    public String updateBook(@PathVariable Long id,
            @RequestParam String title,
            @RequestParam String author,
            @RequestParam Double price,
            @RequestParam(name = "genre", required = false) String genre,
            @RequestParam(name = "summary", required = false) String summary,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {

        Book book = bookService.getBookById(id);
        if (book == null)
            return "redirect:/admin/dashboard?error=BookNotFound";

        book.setTitle(title);
        book.setAuthor(author);
        book.setPrice(price);
        book.setGenre((genre != null && !genre.isEmpty()) ? genre : "Unknown");
        book.setSummary(summary); // ✅ Set summary

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
                Path path = Paths.get("src/main/resources/static/images/" + fileName);
                Files.copy(imageFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                book.setImagePath("/images/" + fileName);
            } catch (IOException e) {
                e.printStackTrace();
                return "redirect:/admin/dashboard?error=ImageUploadFailed";
            }
        }

        bookService.updateBook(book);
        return "redirect:/admin/dashboard?success=BookUpdated";
    }

    @GetMapping("/book/{id}")
    public String viewBookDetails(
            @PathVariable Long id,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "filter", required = false) String filter,
            Model model,
            HttpSession session) {

        if (session.getAttribute("adminName") == null)
            return "redirect:/login";

        Book book = bookService.getBookById(id);
        if (book == null)
            return "redirect:/admin/dashboard?error=BookNotFound";

        List<Review> reviews = reviewService.getSortedAndFilteredReviews(book, sort, filter);
        Double avgRating = reviewService.getAverageRatingForBook(book);

        model.addAttribute("book", book);
        model.addAttribute("reviews", reviews);
        model.addAttribute("avgRating", avgRating);
        model.addAttribute("sort", sort);
        model.addAttribute("filter", filter);

        return "admin-book-details";
    }

    @GetMapping("/manage-books")
    public String manageBooks(
            @RequestParam(value = "search", required = false) String search,
            HttpSession session,
            Model model) {
        String adminName = (String) session.getAttribute("adminName");
        if (adminName == null)
            return "redirect:/login";

        List<Book> books = (search != null && !search.isEmpty()) 
            ? bookService.searchBooks(search)
            : bookService.getAllBooks();

        model.addAttribute("books", books);
        model.addAttribute("search", search);
        model.addAttribute("adminName", adminName);
        return "manage-books";
    }

    @GetMapping("/user/{id}")
    public String viewUserDetails(
            @PathVariable Long id,
            @RequestParam(value = "sort", required = false) String sort,
            Model model,
            HttpSession session) {

        if (session.getAttribute("adminName") == null)
            return "redirect:/login";

        User user = userService.getUserById(id);
        if (user == null)
            return "redirect:/admin/manage-users?error=UserNotFound";

        // Get all reviews by the user
        List<Review> reviews = reviewService.getReviewsByUser(user);

        // Sort reviews if requested
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

        model.addAttribute("user", user);
        model.addAttribute("reviews", reviews);
        model.addAttribute("sort", sort);

        return "admin-user-details";
    }

    @PostMapping("/delete-book/{id}")
    public String deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return "redirect:/admin/manage-books";
    }

    @PostMapping("/delete-review/{reviewId}")
    public String deleteReview(@PathVariable Long reviewId, HttpSession session) {
        String adminName = (String) session.getAttribute("adminName");
        if (adminName == null) {
            return "redirect:/login";
        }

        reviewService.deleteReviewById(reviewId);
        return "redirect:/admin/manage-users?success=ReviewDeleted";
    }
}
