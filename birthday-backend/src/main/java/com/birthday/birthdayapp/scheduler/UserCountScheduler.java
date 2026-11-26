package com.birthday.birthdayapp.scheduler;

import com.birthday.birthdayapp.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class UserCountScheduler {

    private final UserRepository userRepository;

    public UserCountScheduler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Runs every 5 minutes (300,000 ms) while backend is running
    @Scheduled(fixedRate = 300000)
    public void showUserCount() {
        long count = userRepository.count();

        System.out.println("================================");
        System.out.println("Birthday Wishes Application");
        System.out.println("================================");
        System.out.println("Total registered users: " + count);
        System.out.println("Server is running successfully!");
        System.out.println("================================");
    }
 }
