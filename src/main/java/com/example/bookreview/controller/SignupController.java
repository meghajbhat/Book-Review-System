package com.example.bookreview.controller;

import com.example.bookreview.model.User;
import com.example.bookreview.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class SignupController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {
        // 🔥 Check if the username, email, or phone number already exists
        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
        Optional<User> existingEmail = userRepository.findByEmail(user.getEmail());
        Optional<User> existingPhone = userRepository.findByPhoneNumber(user.getPhoneNumber());

        if (existingUser.isPresent()) {
            model.addAttribute("error", "⚠️ Username already exists! Try a different one.");
            return "signup";
        }

        if (existingEmail.isPresent()) {
            model.addAttribute("error", "⚠️ Email already registered! Please log in.");
            return "signup";
        }

        if (existingPhone.isPresent()) {
            model.addAttribute("error", "⚠️ Phone number already in use! Please log in.");
            return "signup";
        }

        // 🔥 Validate phone number format (10-digit numbers only)
        String phone = String.valueOf(user.getPhoneNumber());
        if (!phone.matches("\\d{10}")) { // Ensures only 10-digit numbers are accepted
            model.addAttribute("error", "⚠️ Phone number must be exactly 10 digits.");
            return "signup";
        }

        // ✅ Save new user
        userRepository.save(user);
        System.out.println("✅ User registered successfully!");
        model.addAttribute("success", "🎉 Account created! Please log in.");
        return "redirect:/login";
    }

}
