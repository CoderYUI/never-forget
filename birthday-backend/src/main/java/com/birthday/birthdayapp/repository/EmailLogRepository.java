package com.birthday.birthdayapp.repository;

import com.birthday.birthdayapp.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {

    // Checks if a birthday email was already successfully sent to this user for the given year
    boolean existsByUserIdAndEmailTypeAndBirthdayYearAndStatus(
            Long userId, String emailType, Integer birthdayYear, String status
    );

    // Finds an existing log for this user, email type, and year (whether SENT or FAILED)
    Optional<EmailLog> findByUserIdAndEmailTypeAndBirthdayYear(
            Long userId, String emailType, Integer birthdayYear
    );

    // Finds failed birthday emails that haven't exceeded max retry attempts (e.g. 3 attempts)
    List<EmailLog> findByStatusAndRetryCountLessThan(String status, int maxRetries);
}
