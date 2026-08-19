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
        return userRepository.save(user);
    }

    // ── Login ─────────────────────────────────────────────────────

    public User login(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())) {
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

    // ── Internal: SNS publish ─────────────────────────────────────

    private void publishUserUpdatedEvent(UserUpdatedEvent event) {
        snsTemplate.sendNotification(topicArn, event, "user-updated");
        log.info("Published user-updated event to SNS for: {}", event.getEmail());
    }
}
