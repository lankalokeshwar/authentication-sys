package com.example.Authentication_sys.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory OTP store with a 10-minute expiry.
 * Keyed by email address.
 */
@Component
public class OtpStore {

    private static final long EXPIRY_SECONDS = 600; // 10 minutes
    private static final SecureRandom RANDOM = new SecureRandom();

    private record Entry(String otp, Instant expiresAt) {}

    private final ConcurrentHashMap<String, Entry> store = new ConcurrentHashMap<>();

    /** Generates a 6-digit OTP, stores it, and returns it. */
    public String generate(String email) {
        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
        store.put(email, new Entry(otp, Instant.now().plusSeconds(EXPIRY_SECONDS)));
        return otp;
    }

    /** Returns true if the OTP matches and has not expired. Clears it on success. */
    public boolean verify(String email, String otp) {
        Entry entry = store.get(email);
        if (entry == null) return false;
        if (Instant.now().isAfter(entry.expiresAt())) {
            store.remove(email);
            return false;
        }
        if (!entry.otp().equals(otp)) return false;
        store.remove(email); // one-time use
        return true;
    }
}
