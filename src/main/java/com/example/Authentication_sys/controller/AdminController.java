package com.example.Authentication_sys.controller;

import com.example.Authentication_sys.dto.Requests.AdminChangeEmailRequest;
import com.example.Authentication_sys.dto.Requests.AdminChangePasswordRequest;
import com.example.Authentication_sys.dto.Requests.AdminUpdateRequest;
import com.example.Authentication_sys.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // ── Admin profile ─────────────────────────────────────────────

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return adminService.getAdminProfile(email)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/profile/email")
    public ResponseEntity<?> changeEmail(@Valid @RequestBody AdminChangeEmailRequest req) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        String error = adminService.changeEmail(email, req.newEmail, req.password);
        if (error == null)
            return ResponseEntity.ok(Map.of("success", true, "message", "Email updated. Please log in again."));
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", error));
    }

    @PutMapping("/profile/password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody AdminChangePasswordRequest req) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (adminService.changePassword(email, req.currentPassword, req.newPassword))
            return ResponseEntity.ok(Map.of("success", true, "message", "Password updated successfully."));
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Current password is incorrect."));
    }

    // ── User management ───────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/search")
    public ResponseEntity<?> searchUsers(@RequestParam String q) {
        return ResponseEntity.ok(adminService.searchUsers(q));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(@PathVariable String id) {
        return adminService.getUserById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @Valid @RequestBody AdminUpdateRequest req) {
        return adminService.updateUser(id, req)
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(Map.of("success", true, "user", u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        if (adminService.deleteUser(id))
            return ResponseEntity.ok(Map.of("success", true, "message", "User deleted"));
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/users/{id}/toggle-block")
    public ResponseEntity<?> toggleBlock(@PathVariable String id) {
        return adminService.toggleBlock(id)
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(Map.of(
                        "success", true,
                        "blocked", u.isBlocked(),
                        "message", u.isBlocked() ? "User blocked" : "User unblocked"
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}
