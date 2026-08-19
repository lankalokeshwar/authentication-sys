package com.example.Authentication_sys.exception;

/**
 * All custom exceptions as nested classes.
 * Use as AppExceptions.UserNotFoundException, etc.
 */
public class AppExceptions {

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    public static class InvalidOtpException extends RuntimeException {
        public InvalidOtpException(String message) {
            super(message);
        }
    }
}
