package com.birthday.birthdayapp.controller;

import com.birthday.birthdayapp.repository.UserRepository;
import com.birthday.birthdayapp.service.BirthdayService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/internal")
public class AutomationController {

    @Value("${app.cron.secret}")
    private String configuredSecret;

    private final BirthdayService birthdayService;
    private final UserRepository userRepository;

    public AutomationController(BirthdayService birthdayService, UserRepository userRepository) {
        this.birthdayService = birthdayService;
        this.userRepository = userRepository;
    }

    @PostMapping("/run")
    public ResponseEntity<Map<String, Object>> runAutomation(
            @RequestHeader(value = "X-Cron-Secret", required = false) String incomingSecret
    ) {
        if (configuredSecret != null && !configuredSecret.isBlank() && !configuredSecret.equals(incomingSecret)) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Unauthorized: Invalid or missing X-Cron-Secret header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        System.out.println("\n=======================================================");
        System.out.println(" AUTOMATION TRIGGERED: Checking Birthdays & Retries");
        System.out.println("=======================================================");

        birthdayService.processDailyBirthdays();

        long totalUsers = userRepository.count();
        System.out.println("Total registered users: " + totalUsers);
        System.out.println("=======================================================\n");

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Daily birthday automation completed successfully");
        response.put("totalUsers", totalUsers);

        return ResponseEntity.ok(response);
    }
}