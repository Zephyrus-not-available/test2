
package com.KTU.KTUVotingapp.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${voting.user-pin:12345}")
    private String userPin;

    @Value("${voting.admin-pin:99999}")
    private String adminPin;

    @PostMapping("/verify-pin")
    public ResponseEntity<?> verifyPin(@RequestBody PinRequest request) {
        if (request == null || request.getPin() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Missing pin"));
        }

        String pin = request.getPin().trim();
        if (pin.equals(userPin) || pin.equals(adminPin)) {
            return ResponseEntity.ok(Map.of("valid", true, "alreadyVoted", false));
        }

        // Front-end expects 404 to mean "PIN does not exist"
        return ResponseEntity.status(404).body("Pin not found");
    }

    public static class PinRequest {
        private String pin;
        public String getPin() { return pin; }
        public void setPin(String pin) { this.pin = pin; }
    }
}