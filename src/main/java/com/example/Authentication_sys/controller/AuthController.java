package com.example.Authentication_sys.controller;

import com.example.Authentication_sys.config.JwtUtil;
import com.example.Authentication_sys.dto.Requests;
import com.example.Authentication_sys.model.User;
import com.example.Authentication_sys.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UserService userService;
    @Autowired private JwtUtil jwtUtil;

    // ── Register ──────────────────────────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody Requests.RegisterRequest req) {
        User user = new User();
        user.setFirstName(req.firstName);
        user.setLastName(req.lastName);
        user.setPhoneNumber(req.phoneNumber);
        user.setEmail(req.email);
        user.setPassword(passwordEncoder.encode(req.password));
        user.setDateOfBirth(req.dateOfBirth);
        user.setGender(req.gender);

        Object result = userService.register(user);

        if (result == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("success", false, "message", "An account with this email already exists"));
        }

        User savedUser = (User) result;
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "token", jwtUtil.generate(savedUser.getEmail(), savedUser.getRole()),
                "user", savedUser,
                "message", "Account created successfully."
        ));
    }

    // ── Login ─────────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody Requests.LoginRequest req) {
        User user = userService.login(req.email, req.password);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Invalid credentials"));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "token", jwtUtil.generate(user.getEmail(), user.getRole()),
                "displayName", user.getFullName(),
                "user", user
        ));
    }

    // ── Update profile ────────────────────────────────────────────

    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> update(@Valid @RequestBody Requests.UpdateRequest req) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        boolean accepted = userService.updateUser(
                email,
                req.firstName, req.lastName, req.phoneNumber,
                req.dateOfBirth, req.gender);

        if (!accepted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "User not found"));
        }
        return ResponseEntity.ok(Map.of("success", true, "message", "Profile updated successfully."));
    }

    // ── Forgot / reset password ───────────────────────────────────

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@Valid @RequestBody Requests.ForgotPasswordRequest req) {
        userService.forgotPassword(req.email);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "OTP sent to " + req.email + ". Valid for 10 minutes."
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody Requests.ResetPasswordRequest req) {
        userService.resetPassword(req.email, req.otp, req.newPassword);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password reset successfully. You can now log in."
        ));
    }
}
