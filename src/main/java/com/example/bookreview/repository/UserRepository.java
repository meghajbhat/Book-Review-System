package com.example.bookreview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import com.example.bookreview.model.User;
import java.util.Optional;
import java.util.List;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    // 🔹 Find by Specific Fields
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(Long phoneNumber);

    // 🔹 Find by Name (Case-Insensitive Search)
    List<User> findByNameContainingIgnoreCase(String name);

    // 🔹 Disable User (Soft Delete)
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.status = 'DISABLED' WHERE u.userId = :id")
    void disableUser(@Param("id") Long id);

    // 🔹 Delete User (Permanent)
    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.userId = :id")
    void deleteById(@Param("id") Long id);

    // �� Enable User (Soft Enable)
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.status = 'ACTIVE' WHERE u.userId = :id")
    void enableUser(@Param("id") Long id);

    // 🔍 Search Users by Name, Username, Email, Phone, Status, or Role
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "CAST(u.phoneNumber AS string) LIKE CONCAT('%', :keyword, '%') OR " +
            "LOWER(u.status) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.role) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchUsers(@Param("keyword") String keyword);
}
