package com.example.bookreview.service;

import com.example.bookreview.model.User;
import com.example.bookreview.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // 🔹 Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 🔹 Find user by username
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // 🔹 Add a new user
    public User addUser(User user) {
        return userRepository.save(user);
    }

    // 🔹 Disable a user (Soft Delete)
    public void disableUser(Long id) {
        userRepository.disableUser(id);
    }

    // 🔹 Permanently delete a user
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // 🔍 Search Users by Any Field (Name, Username, Email, Phone, Status, Role)
    public List<User> searchUsers(String keyword) {
        return userRepository.searchUsers(keyword);
    }

    // enable user
    public void enableUser(Long id) {
        userRepository.enableUser(id);
    }

    // Get user by ID
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

}
