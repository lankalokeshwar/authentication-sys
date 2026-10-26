package com.example.Authentication_sys.service;

import com.example.Authentication_sys.dto.Requests.AdminUpdateRequest;
import com.example.Authentication_sys.model.User;
import com.example.Authentication_sys.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    // ── Admin profile ─────────────────────────────────────────────

    public Optional<User> getAdminProfile(String email) {
        return userRepository.findByEmail(email);
    }

    public String changeEmail(String currentEmail, String newEmail, String password) {
        User admin = userRepository.findByEmail(currentEmail).orElse(null);
        if (admin == null) return "Admin not found.";
        if (!passwordEncoder.matches(password, admin.getPassword())) return "Incorrect password.";
        if (userRepository.existsByEmail(newEmail)) return "This email is already in use.";
        admin.setEmail(newEmail);
        userRepository.save(admin);
        return null; // null = success
    }

    public boolean changePassword(String email, String currentPassword, String newPassword) {
        User admin = userRepository.findByEmail(email).orElse(null);
        if (admin == null) return false;
        if (!passwordEncoder.matches(currentPassword, admin.getPassword())) return false;
        admin.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(admin);
        return true;
    }

        // ── User list (exclude admins) ────────────────────────────────

    public List<User> getAllUsers() {
        return userRepository.findAll().stream()
                .filter(u -> !"ADMIN".equalsIgnoreCase(u.getRole()))
                .toList();
    }

    public List<User> searchUsers(String query) {
        return userRepository.search(query).stream()
                .filter(u -> !"ADMIN".equalsIgnoreCase(u.getRole()))
                .toList();
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> updateUser(String id, AdminUpdateRequest req) {
        return userRepository.findById(id).map(user -> {
            user.setFirstName(req.firstName);
            user.setLastName(req.lastName);
            user.setPhoneNumber(req.phoneNumber);
            user.setDateOfBirth(req.dateOfBirth);
            user.setGender(req.gender);
            return userRepository.save(user);
        });
    }

    public boolean deleteUser(String id) {
        if (!userRepository.existsById(id)) return false;
        userRepository.deleteById(id);
        return true;
    }

    public Optional<User> toggleBlock(String id) {
        return userRepository.findById(id).map(user -> {
            user.setBlocked(!user.isBlocked());
            return userRepository.save(user);
        });
    }
}
