package com.example.bookreview.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
    @GetMapping("/")
    public String home() {
        return "signup"; // Loads signup.html from src/main/resources/templates/
    }
}
