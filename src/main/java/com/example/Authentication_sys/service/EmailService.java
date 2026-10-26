package com.example.Authentication_sys.service;

import com.example.Authentication_sys.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationOtp(String toEmail, String firstName, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Verify Your Email Address");
        message.setText(
                "Hi " + firstName + ",\n\n" +
                "Thank you for registering!\n\n" +
                "Your email verification OTP is: " + otp + "\n\n" +
                "This OTP is valid for 10 minutes. Do not share it with anyone.\n\n" +
                "If you did not create an account, please ignore this email.\n\n" +
                "Regards,\nThe Auth Team"
        );
        mailSender.send(message);
    }

    public void sendChangeEmailOtp(String toEmail, String firstName, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Verify Your New Email Address");
        message.setText(
                "Hi " + firstName + ",\n\n" +
                "We received a request to change your email address to this one.\n\n" +
                "Your verification OTP is: " + otp + "\n\n" +
                "This OTP is valid for 10 minutes. Do not share it with anyone.\n\n" +
                "If you did not request this change, please ignore this email.\n\n" +
                "Regards,\nThe Auth Team"
        );
        mailSender.send(message);
    }

    public void sendPasswordResetOtp(String toEmail, String firstName, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your Password Reset OTP");
        message.setText(
                "Hi " + firstName + ",\n\n" +
                "We received a request to reset your password.\n\n" +
                "Your OTP is: " + otp + "\n\n" +
                "This OTP is valid for 10 minutes. Do not share it with anyone.\n\n" +
                "If you did not request a password reset, please ignore this email.\n\n" +
                "Regards,\nThe Auth Team"
        );
        mailSender.send(message);
    }

    public void sendProfileUpdateEmail(User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Your profile has been updated");
        message.setText(
                "Hi " + user.getFirstName() + ",\n\n" +
                "Your profile was successfully updated. Here are your current details:\n\n" +
                "First Name   : " + user.getFirstName() + "\n" +
                "Last Name    : " + user.getLastName() + "\n" +
                "Phone Number : " + user.getPhoneNumber() + "\n" +
                "Date of Birth: " + (user.getDateOfBirth() != null ? user.getDateOfBirth() : "—") + "\n" +
                "Gender       : " + (user.getGender() != null ? user.getGender() : "—") + "\n\n" +
                "If you did not make this change, please contact support immediately.\n\n" +
                "Regards,\nThe Auth Team"
        );
        mailSender.send(message);
    }
}
