package com.birthday.birthdayapp.controller;

import com.birthday.birthdayapp.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/health")

public class HealthController {

    private final UserRepository userRepository;

    // A collection of cute, heartwarming quotes
    private static final List<String> CUTE_QUOTES = List.of(
            "Every day is a gift, and every birthday is a new chapter! 🎂",
            "Count your life by smiles, not tears. Count your age by friends, not years. ♡",
            "May your days be sprinkled with extra love, belly laughs, and sweet surprises. ✨",
            "A tiny reminder: The world is brighter with you in it! 🌸",
            "Sending a little warm sunshine your way today! ☀️",
            "Keep smiling, spreading kindness, and eating cake! 🍰"
    );

    public HealthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        long userCount = userRepository.count();
        String randomQuote = CUTE_QUOTES.get(ThreadLocalRandom.current().nextInt(CUTE_QUOTES.size()));

        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Birthday server is awake and healthy! ♡");
        response.put("totalUsers", userCount);
        response.put("quote", randomQuote);

        return ResponseEntity.ok(response);
    }
}