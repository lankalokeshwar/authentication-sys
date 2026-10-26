package com.example.Authentication_sys.service;

import com.example.Authentication_sys.dto.UserUpdatedEvent;
import com.example.Authentication_sys.exception.AppExceptions;
import com.example.Authentication_sys.model.User;
import com.example.Authentication_sys.repository.UserRepository;
import io.awspring.cloud.sns.core.SnsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UserRepository userRepository;
    @Autowired private SnsTemplate snsTemplate;
    @Autowired private OtpStore otpStore;
    @Autowired private EmailService emailService;

    @Value("${aws.sns.topicArn}")
    private String topicArn;

    // ── Registration ──────────────────────────────────────────────

    public Object register(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            return null;
        }
        user.setRole("USER");
        user.setVerified(false);
        User saved = userRepository.save(user);
        // Send verification OTP
        String otp = otpStore.generate(saved.getEmail());
        emailService.sendVerificationOtp(saved.getEmail(), saved.getFirstName(), otp);
        log.info("Verification OTP sent to: {}", saved.getEmail());
        return saved;
    }

    // ── Verify email ──────────────────────────────────────────────

    public boolean verifyEmail(String email, String otp) {
        if (!otpStore.verify(email, otp)) return false;
        User user = getByEmail(email);
        user.setVerified(true);
        userRepository.save(user);
        log.info("Email verified for: {}", email);
        return true;
    }

    public void resendVerificationOtp(String email) {
        User user = getByEmail(email);
        if (user.isVerified()) return;
        String otp = otpStore.generate(email);
        emailService.sendVerificationOtp(email, user.getFirstName(), otp);
        log.info("Verification OTP resent to: {}", email);
    }

    // ── Login ─────────────────────────────────────────────────────

    public User login(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())) { // handling NoSuchElementException
            if (user.get().isBlocked())
                throw new AppExceptions.UserBlockedException("Your account has been blocked. Please contact support.");
            if (!user.get().isVerified())
                throw new AppExceptions.EmailNotVerifiedException("Please verify your email before logging in.");
            return user.get();
        }
        return null;
    }

    // ── Lookup ────────────────────────────────────────────────────

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppExceptions.UserNotFoundException("User not found"));
    }

    // ── Profile update (async via SNS/SQS) ───────────────────────

    public boolean updateUser(String email, String firstName, String lastName,
                              String phoneNumber, String dateOfBirth, String gender) {
        if (!userRepository.existsByEmail(email)) return false;

        publishUserUpdatedEvent(new UserUpdatedEvent(
                email, firstName, lastName, phoneNumber, dateOfBirth, gender));
        log.info("Update event published for: {}", email);
        return true;
    }

    // ── Internal: SNS publish ─────────────────────────────────────

        private void publishUserUpdatedEvent(UserUpdatedEvent event) {
        snsTemplate.sendNotification(topicArn, event, "user-updated");
        log.info("Published user-updated event to SNS for: {}", event.getEmail());
    }

    // ── Change email ──────────────────────────────────────────────

    // Step 1: verify password, send OTP to new email
    public String initiateChangeEmail(String currentEmail, String newEmail, String password) {
        User user = userRepository.findByEmail(currentEmail).orElse(null);
        if (user == null) return "User not found.";
        if (!passwordEncoder.matches(password, user.getPassword())) return "Incorrect password.";
        if (userRepository.existsByEmail(newEmail)) return "This email is already in use.";
        // Send OTP to the NEW email to confirm ownership
        String otp = otpStore.generate(newEmail);
        emailService.sendChangeEmailOtp(newEmail, user.getFirstName(), otp);
        log.info("Change-email OTP sent to new address: {}", newEmail);
        return null; // null = success
    }

    // Step 2: verify OTP then update email
    public String confirmChangeEmail(String currentEmail, String newEmail, String otp) {
        if (!otpStore.verify(newEmail, otp)) return "Invalid or expired OTP.";
        User user = userRepository.findByEmail(currentEmail).orElse(null);
        if (user == null) return "User not found.";
        if (userRepository.existsByEmail(newEmail)) return "This email is already in use.";
        user.setEmail(newEmail);
        userRepository.save(user);
        log.info("Email changed for user: {} -> {}", currentEmail, newEmail);
        return null; // null = success
    }
    // ── Forgot / reset password ───────────────────────────────────

    public void forgotPassword(String email) {
        User user = getByEmail(email);
        String otp = otpStore.generate(email);
        emailService.sendPasswordResetOtp(email, user.getFirstName(), otp);
        log.info("Password reset OTP sent to: {}", email);
    }

    public void resetPassword(String email, String otp, String newPassword) {
        if (!otpStore.verify(email, otp)) {
            throw new AppExceptions.InvalidOtpException("Invalid or expired OTP");
        }
        User user = getByEmail(email);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password reset successful for: {}", email);
    }

}
