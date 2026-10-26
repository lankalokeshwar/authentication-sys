package com.example.Authentication_sys.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class Requests {

    public static class RegisterRequest {
        @NotBlank(message = "First name is required")
        public String firstName;

        @NotBlank(message = "Last name is required")
        public String lastName;

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid phone number")
        public String phoneNumber;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        public String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        public String password;

        // Optional profile fields
        public String dateOfBirth;
        public String gender;
    }

    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        public String email;

        @NotBlank(message = "Password is required")
        public String password;
    }

    public static class UpdateRequest {
        @NotBlank(message = "First name is required")
        public String firstName;

        @NotBlank(message = "Last name is required")
        public String lastName;

        @Pattern(regexp = "^$|^[6-9]\\d{9}$",
                 message = "Invalid phone number (must be 10 digits starting with 6-9)")
        public String phoneNumber;

        public String dateOfBirth;
        public String gender;
    }

    public static class ForgotPasswordRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        public String email;
    }

    public static class AdminUpdateRequest {
        @NotBlank(message = "First name is required")
        public String firstName;

        @NotBlank(message = "Last name is required")
        public String lastName;

        @Pattern(regexp = "^$|^[6-9]\\d{9}$", message = "Invalid phone number")
        public String phoneNumber;

        public String dateOfBirth;
        public String gender;
        public String role;
    }

    public static class ChangeEmailRequest {
        @NotBlank(message = "New email is required")
        @Email(message = "Invalid email format")
        public String newEmail;

        @NotBlank(message = "Password is required")
        public String password;
    }

    public static class VerifyEmailRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        public String email;

        @NotBlank(message = "OTP is required")
        public String otp;
    }

    public static class VerifyChangeEmailRequest {
        @NotBlank(message = "New email is required")
        @Email(message = "Invalid email format")
        public String newEmail;

        @NotBlank(message = "OTP is required")
        public String otp;
    }

    public static class ResendOtpRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        public String email;
    }

    public static class AdminChangeEmailRequest {
        @NotBlank(message = "New email is required")
        @Email(message = "Invalid email format")
        public String newEmail;

        @NotBlank(message = "Password is required")
        public String password;
    }

    public static class AdminChangePasswordRequest {
        @NotBlank(message = "Current password is required")
        public String currentPassword;

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        public String newPassword;
    }

    public static class ResetPasswordRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        public String email;

        @NotBlank(message = "OTP is required")
        public String otp;

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        public String newPassword;
    }
}
