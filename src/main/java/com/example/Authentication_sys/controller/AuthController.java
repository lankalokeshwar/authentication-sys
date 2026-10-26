package com.example.Authentication_sys.controller;

import com.example.Authentication_sys.config.JwtUtil;
import com.example.Authentication_sys.dto.Requests;
import com.example.Authentication_sys.model.User;
import com.example.Authentication_sys.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UserService userService;
    @Autowired private JwtUtil jwtUtil;

    // ── Register ──────────────────────────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody Requests.RegisterRequest req) {
        log.info("Register attempt for email: {}", req.email);
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
            log.warn("Register failed - email already exists: {}", req.email);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("success", false, "message", "An account with this email already exists"));
        }

        log.info("Register successful, OTP sent to: {}", req.email);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "OTP sent to " + req.email + ". Please verify your email."
        ));
    }

    // ── Verify email after registration ───────────────────────────

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, Object>> verifyEmail(@Valid @RequestBody Requests.VerifyEmailRequest req) {
        log.info("Email verification attempt for: {}", req.email);
        if (!userService.verifyEmail(req.email, req.otp)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Invalid or expired OTP."));
        }
        User user = userService.getByEmail(req.email);
        log.info("Email verified for: {}", req.email);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "token", jwtUtil.generate(user.getEmail(), user.getRole()),
                "user", user,
                "message", "Email verified successfully."
        ));
    }

    // ── Resend verification OTP ───────────────────────────────────

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, Object>> resendVerification(@Valid @RequestBody Requests.ResendOtpRequest req) {
        userService.resendVerificationOtp(req.email);
        return ResponseEntity.ok(Map.of("success", true, "message", "OTP resent to " + req.email));
    }

    // ── Login ─────────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody Requests.LoginRequest req) {
        log.info("Login attempt for email: {}", req.email);
        try {
            User user = userService.login(req.email, req.password);
            if (user == null) {
                log.warn("Login failed - invalid credentials for email: {}", req.email);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Invalid credentials"));
            }
            log.info("Login successful for email: {}", req.email);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "token", jwtUtil.generate(user.getEmail(), user.getRole()),
                    "displayName", user.getFullName(),
                    "user", user
            ));
        } catch (com.example.Authentication_sys.exception.AppExceptions.UserBlockedException ex) {
            log.warn("Login blocked for email: {}", req.email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    // ── Update profile ────────────────────────────────────────────

    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> update(@Valid @RequestBody Requests.UpdateRequest req) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Profile update request for email: {}", email);

        boolean accepted = userService.updateUser(
                email,
                req.firstName, req.lastName, req.phoneNumber,
                req.dateOfBirth, req.gender);

        if (!accepted) {
            log.warn("Profile update failed - user not found: {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "User not found"));
        }
        log.info("Profile update successful for email: {}", email);
        return ResponseEntity.ok(Map.of("success", true, "message", "Profile updated successfully."));
    }

    // ── Change email — Step 1: verify password, send OTP ─────────

    @PostMapping("/change-email/initiate")
    public ResponseEntity<Map<String, Object>> initiateChangeEmail(@Valid @RequestBody Requests.ChangeEmailRequest req) {
        String currentEmail = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String error = userService.initiateChangeEmail(currentEmail, req.newEmail, req.password);
        if (error == null)
            return ResponseEntity.ok(Map.of("success", true,
                    "message", "OTP sent to " + req.newEmail + ". Please verify."));
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", error));
    }

    // ── Change email — Step 2: verify OTP, update email ──────────

    @PostMapping("/change-email/confirm")
    public ResponseEntity<Map<String, Object>> confirmChangeEmail(@Valid @RequestBody Requests.VerifyChangeEmailRequest req) {
        String currentEmail = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String error = userService.confirmChangeEmail(currentEmail, req.newEmail, req.otp);
        if (error == null) {
            log.info("Email changed confirmed for: {}", currentEmail);
            return ResponseEntity.ok(Map.of("success", true,
                    "message", "Email updated successfully. Please log in again."));
        }
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", error));
    }

    // ── Forgot / reset password ───────────────────────────────────

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@Valid @RequestBody Requests.ForgotPasswordRequest req) {
        log.info("Forgot password request for email: {}", req.email);
        userService.forgotPassword(req.email);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "OTP sent to " + req.email + ". Valid for 10 minutes."
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody Requests.ResetPasswordRequest req) {
        log.info("Reset password attempt for email: {}", req.email);
        userService.resetPassword(req.email, req.otp, req.newPassword);
        log.info("Reset password successful for email: {}", req.email);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password reset successfully. You can now log in."
        ));
    }
}
