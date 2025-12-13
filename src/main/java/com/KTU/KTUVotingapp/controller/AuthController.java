package com.KTU.KTUVotingapp.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;

import com.KTU.KTUVotingapp.service.RateLimitService;
import com.KTU.KTUVotingapp.service.VotingService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${voting.user-pin:12345}")
    private String userPin;

    @Value("${voting.admin-pin:99999}")
    private String adminPin;

    private final VotingService votingService;
    private final RateLimitService rateLimitService;

    public AuthController(VotingService votingService, RateLimitService rateLimitService) {
        this.votingService = votingService;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping("/verify-pin")
    public ResponseEntity<?> verifyPin(@RequestBody Map<String, Object> body,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        if (body == null || !body.containsKey("pin") || body.get("pin") == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Missing pin"));
        }

        // Get client IP for rate limiting
        String clientIp = getClientIpAddress(request);

        // Check rate limit
        RateLimitService.RateLimitResult rateLimitResult = rateLimitService.checkRateLimit(clientIp);
        if (!rateLimitResult.isAllowed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of(
                    "message", "Too many attempts. Please try again in " + rateLimitResult.getRetryAfterSeconds() + " seconds.",
                    "retryAfter", rateLimitResult.getRetryAfterSeconds()
                ));
        }

        String pin = String.valueOf(body.get("pin")).trim();

        // Get or create device ID from cookie or IP hash
        String deviceId = getOrCreateDeviceId(request, response);

        // Check if this device has already voted
        boolean deviceAlreadyVoted = votingService.deviceHasVoted(deviceId);

        if (pin.equals(userPin)) {
            // Record successful attempt (resets rate limit)
            rateLimitService.recordAttempt(clientIp, true);

            return ResponseEntity.ok(Map.of(
                "valid", true,
                "alreadyVoted", deviceAlreadyVoted,
                "role", "user",
                "deviceId", deviceId,
                "remainingAttempts", rateLimitResult.getRemainingAttempts()
            ));
        }
        if (pin.equals(adminPin)) {
            // Record successful attempt
            rateLimitService.recordAttempt(clientIp, true);

            // Admins can always access even if device voted
            return ResponseEntity.ok(Map.of(
                "valid", true,
                "alreadyVoted", false,
                "role", "admin",
                "deviceId", deviceId
            ));
        }

        // Record failed attempt
        rateLimitService.recordAttempt(clientIp, false);

        // Get updated rate limit info after failed attempt
        RateLimitService.RateLimitResult updatedRateLimit = rateLimitService.checkRateLimit(clientIp);

        // Front-end expects 404 to mean "PIN does not exist"
        return ResponseEntity.status(404).body(Map.of(
            "message", "Pin not found",
            "remainingAttempts", updatedRateLimit.getRemainingAttempts()
        ));
    }

    /**
     * Check if current device has already voted
     */
    @GetMapping("/check-device")
    public ResponseEntity<?> checkDevice(HttpServletRequest request, HttpServletResponse response) {
        String deviceId = getOrCreateDeviceId(request, response);
        boolean hasVoted = votingService.deviceHasVoted(deviceId);
        return ResponseEntity.ok(Map.of(
            "deviceId", deviceId,
            "hasVoted", hasVoted
        ));
    }

    /**
     * Get or create a persistent device ID using IP-only hash.
     * This ensures the same device ID across all browsers and incognito modes.
     *
     * IMPORTANT: Uses IP-only (not User-Agent) to prevent voting from:
     * - Different browsers on the same device
     * - Incognito/private browsing mode
     * - Clearing cookies
     */
    private String getOrCreateDeviceId(HttpServletRequest request, HttpServletResponse response) {
        // Always use IP-only based ID for consistency across browsers/incognito
        String ipOnlyId = deriveIpOnlyId(request);

        // Still set cookie for faster lookups on subsequent requests
        Cookie deviceCookie = new Cookie("voting_device_id", ipOnlyId);
        deviceCookie.setMaxAge(365 * 24 * 60 * 60); // 1 year
        deviceCookie.setPath("/");
        deviceCookie.setHttpOnly(true);
        deviceCookie.setSecure(request.isSecure());
        response.addCookie(deviceCookie);

        return ipOnlyId;
    }

    private String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Derive device ID from IP address only.
     * This ensures same ID for all browsers/incognito on same network.
     */
    private String deriveIpOnlyId(HttpServletRequest request) {
        String ip = getClientIpAddress(request);
        // Use only IP address - consistent across all browsers on same device/network
        String source = (ip == null ? "unknown" : ip);

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(source.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                sb.append(String.format("%02x", hash[i]));
            }
            return "ip-" + sb;
        } catch (NoSuchAlgorithmException e) {
            // Fallback: use IP directly with prefix
            return "ip-" + source.replace(".", "-").replace(":", "-");
        }
    }

    /**
     * Get client IP address considering proxy headers
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_CLIENT_IP"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For can contain multiple IPs, get the first one
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }
}