package com.birthday.birthdayapp.service;

import com.birthday.birthdayapp.entity.EmailLog;
import com.birthday.birthdayapp.entity.User;
import com.birthday.birthdayapp.repository.EmailLogRepository;
import com.birthday.birthdayapp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class BirthdayService {

    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");
    private static final int MAX_RETRIES = 3;

    private final UserRepository userRepository;
    private final EmailLogRepository emailLogRepository;
    private final EmailService emailService;

    public BirthdayService(UserRepository userRepository,
                           EmailLogRepository emailLogRepository,
                           EmailService emailService) {
        this.userRepository = userRepository;
        this.emailLogRepository = emailLogRepository;
        this.emailService = emailService;
    }

    /**
     * Main task: Processes today's birthdays and retries failed emails
     */
    public void processDailyBirthdays() {
        LocalDate todayIst = LocalDate.now(IST_ZONE);
        int currentYear = todayIst.getYear();
        int month = todayIst.getMonthValue();
        int day = todayIst.getDayOfMonth();

        System.out.println("Processing birthdays for date (IST): " + todayIst);

        // 1. Fetch users whose birthday is today
        List<User> birthdayUsers = userRepository.findUsersByBirthdayMonthAndDay(month, day);
        System.out.println("Found " + birthdayUsers.size() + " user(s) celebrating birthdays today!");

        // 2. Send birthday email (if not already sent this year)
        for (User user : birthdayUsers) {
            sendBirthdayEmailToUser(user, currentYear);
        }

        // 3. Retry previously failed attempts
        retryFailedEmails();
    }

    private void sendBirthdayEmailToUser(User user, int currentYear) {
        // Prevent duplicate birthday emails in the same year
        boolean alreadySent = emailLogRepository.existsByUserIdAndEmailTypeAndBirthdayYearAndStatus(
                user.getId(), "BIRTHDAY", currentYear, "SENT"
        );

        if (alreadySent) {
            System.out.println("Birthday email already sent this year to: " + user.getEmail() + " - skipping.");
            return;
        }

        System.out.println("Sending birthday email to: " + user.getEmail());
        boolean success = emailService.sendBirthdayEmail(user.getEmail(), user.getUsername());

        // Update or insert into email_logs
        EmailLog log = emailLogRepository.findByUserIdAndEmailTypeAndBirthdayYear(
                user.getId(), "BIRTHDAY", currentYear
        ).orElse(new EmailLog(user.getId(), "BIRTHDAY", currentYear, "PENDING", 0, null));

        if (success) {
            log.setStatus("SENT");
            log.setSentAt(LocalDateTime.now());
            System.out.println("Birthday email successfully delivered to: " + user.getEmail());
        } else {
            log.setStatus("FAILED");
            log.setRetryCount(log.getRetryCount() + 1);
            System.err.println("Failed to deliver birthday email to: " + user.getEmail() + " (Attempt " + log.getRetryCount() + ")");
        }

        emailLogRepository.save(log);
    }

    private void retryFailedEmails() {
        List<EmailLog> failedLogs = emailLogRepository.findByStatusAndRetryCountLessThan("FAILED", MAX_RETRIES);

        if (failedLogs.isEmpty()) {
            return;
        }

        System.out.println("Found " + failedLogs.size() + " failed email(s) eligible for retry...");

        for (EmailLog log : failedLogs) {
            Optional<User> userOpt = userRepository.findById(log.getUserId());
            if (userOpt.isEmpty()) {
                continue;
            }

            User user = userOpt.get();
            System.out.println("Retrying birthday email for: " + user.getEmail() + " (Attempt " + (log.getRetryCount() + 1) + ")");

            boolean success = emailService.sendBirthdayEmail(user.getEmail(), user.getUsername());
            log.setRetryCount(log.getRetryCount() + 1);

            if (success) {
                log.setStatus("SENT");
                log.setSentAt(LocalDateTime.now());
                System.out.println("Retry SUCCEEDED for: " + user.getEmail());
            } else {
                System.err.println("Retry FAILED for: " + user.getEmail());
            }

            emailLogRepository.save(log);
        }
    }
}