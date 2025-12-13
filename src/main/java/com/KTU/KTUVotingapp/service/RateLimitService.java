package com.KTU.KTUVotingapp.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory rate limiting service to prevent brute force attacks.
 * Limits PIN verification attempts per IP address.
 */
@Service
public class RateLimitService {

    // Maximum attempts per IP within the time window
    private static final int MAX_ATTEMPTS = 5;

    // Time window in seconds
    private static final long WINDOW_SECONDS = 60;

    // Lockout duration in seconds after max attempts exceeded
    private static final long LOCKOUT_SECONDS = 300; // 5 minutes

    // Store attempt counts and timestamps per IP
    private final Map<String, AttemptInfo> attemptMap = new ConcurrentHashMap<>();

    public static class AttemptInfo {
        int count;
        Instant windowStart;
        Instant lockoutUntil;

        AttemptInfo() {
            this.count = 0;
            this.windowStart = Instant.now();
            this.lockoutUntil = null;
        }
    }

    public static class RateLimitResult {
        private final boolean allowed;
        private final int remainingAttempts;
        private final long retryAfterSeconds;

        public RateLimitResult(boolean allowed, int remainingAttempts, long retryAfterSeconds) {
            this.allowed = allowed;
            this.remainingAttempts = remainingAttempts;
            this.retryAfterSeconds = retryAfterSeconds;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public int getRemainingAttempts() {
            return remainingAttempts;
        }

        public long getRetryAfterSeconds() {
            return retryAfterSeconds;
        }
    }

    /**
     * Check if the given IP address is allowed to make a PIN verification attempt.
     *
     * @param ipAddress The client IP address
     * @return RateLimitResult indicating if the request is allowed
     */
    public RateLimitResult checkRateLimit(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return new RateLimitResult(true, MAX_ATTEMPTS, 0);
        }

        AttemptInfo info = attemptMap.computeIfAbsent(ipAddress, k -> new AttemptInfo());
        Instant now = Instant.now();

        synchronized (info) {
            // Check if currently locked out
            if (info.lockoutUntil != null && now.isBefore(info.lockoutUntil)) {
                long retryAfter = info.lockoutUntil.getEpochSecond() - now.getEpochSecond();
                return new RateLimitResult(false, 0, retryAfter);
            }

            // Reset lockout if expired
            if (info.lockoutUntil != null && now.isAfter(info.lockoutUntil)) {
                info.lockoutUntil = null;
                info.count = 0;
                info.windowStart = now;
            }

            // Check if window has expired
            if (now.getEpochSecond() - info.windowStart.getEpochSecond() > WINDOW_SECONDS) {
                // Reset the window
                info.count = 0;
                info.windowStart = now;
            }

            // Check if too many attempts
            if (info.count >= MAX_ATTEMPTS) {
                // Apply lockout
                info.lockoutUntil = now.plusSeconds(LOCKOUT_SECONDS);
                return new RateLimitResult(false, 0, LOCKOUT_SECONDS);
            }

            int remaining = MAX_ATTEMPTS - info.count;
            return new RateLimitResult(true, remaining, 0);
        }
    }

    /**
     * Record a PIN verification attempt for the given IP address.
     *
     * @param ipAddress The client IP address
     * @param successful Whether the attempt was successful (valid PIN)
     */
    public void recordAttempt(String ipAddress, boolean successful) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return;
        }

        AttemptInfo info = attemptMap.computeIfAbsent(ipAddress, k -> new AttemptInfo());

        synchronized (info) {
            if (successful) {
                // Reset on successful attempt
                info.count = 0;
                info.windowStart = Instant.now();
                info.lockoutUntil = null;
            } else {
                // Increment failed attempt counter
                info.count++;
            }
        }
    }

    /**
     * Clear rate limit data for an IP (useful for admin operations).
     *
     * @param ipAddress The IP address to clear
     */
    public void clearRateLimit(String ipAddress) {
        if (ipAddress != null) {
            attemptMap.remove(ipAddress);
        }
    }

    /**
     * Get info about rate limit status (for debugging/admin).
     */
    public Map<String, Object> getRateLimitInfo(String ipAddress) {
        AttemptInfo info = attemptMap.get(ipAddress);
        if (info == null) {
            return Map.of(
                "attempts", 0,
                "maxAttempts", MAX_ATTEMPTS,
                "lockedOut", false
            );
        }

        synchronized (info) {
            Instant now = Instant.now();
            boolean lockedOut = info.lockoutUntil != null && now.isBefore(info.lockoutUntil);
            long retryAfter = lockedOut ?
                (info.lockoutUntil.getEpochSecond() - now.getEpochSecond()) : 0;

            return Map.of(
                "attempts", info.count,
                "maxAttempts", MAX_ATTEMPTS,
                "lockedOut", lockedOut,
                "retryAfterSeconds", retryAfter
            );
        }
    }
}

