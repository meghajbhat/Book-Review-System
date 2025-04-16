package com.example.bookreview.controller;

import com.example.bookreview.model.User;
import com.example.bookreview.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "success", required = false) String success, Model model) {
        if (success != null) {
            model.addAttribute("success", "🎉 Account created! Please log in.");
        }
        return "login"; // Loads login.html from templates
    }

    @PostMapping("/authenticate")
    public String authenticateUser(@RequestParam String username, @RequestParam String password, Model model,
            HttpSession session) {
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isPresent() && user.get().getPassword().equals(password)) {
            session.setAttribute("loggedInUser", user.get()); // ✅ Store user in session

            if (user.get().getRole().toString().equals("ADMIN")) {
                session.setAttribute("adminName", user.get().getUsername()); // ✅ Store admin name
                return "redirect:/admin/dashboard"; // ✅ Redirect to admin dashboard
            } else {
                session.setAttribute("customerName", user.get().getUsername()); // ✅ Store customer name
                return "redirect:/customer-dashboard"; // ✅ Redirect to customer dashboard
            }
        } else {
            model.addAttribute("error", "❌ Invalid username or password!");
            return "login"; // Stay on login page if credentials are wrong
        }
    }

    // ✅ Logout Endpoint
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // ✅ Clears session data
        return "redirect:/login?logout"; // ✅ Redirects to login page
    }
}
