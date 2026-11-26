package com.birthday.birthdayapp.service;

import com.birthday.birthdayapp.dto.RegisterRequest;
import com.birthday.birthdayapp.entity.User;
import com.birthday.birthdayapp.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    // Inject both UserRepository and EmailService
    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public User registerUser(RegisterRequest request) {
        // 1. Sanitize email to lowercase and trim spaces
        String cleanEmail = request.getEmail().trim().toLowerCase();

        // 2. Check unique email constraint
        if (userRepository.existsByEmail(cleanEmail)) {
            throw new IllegalArgumentException("This email is already registered!");
        }

        // 3. Create new user entity and save to Supabase
        User user = new User(
                request.getUsername().trim(),
                cleanEmail,
                request.getCountry().trim(),
                request.getDateOfBirth()
        );

        User savedUser = userRepository.save(user);

        // 4. Trigger the welcome email via Google Apps Script
        try {
            boolean sent = emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getUsername());
            if (sent) {
                System.out.println("Welcome email triggered successfully for: " + savedUser.getEmail());
            } else {
                System.err.println("Google Apps Script failed to send welcome email.");
            }
        } catch (Exception e) {
            System.err.println("Exception while sending welcome email: " + e.getMessage());
        }

        return savedUser;
    }
}