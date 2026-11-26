package com.birthday.birthdayapp.scheduler;

import com.birthday.birthdayapp.service.BirthdayService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BirthdayScheduler {

    private final BirthdayService birthdayService;

    public BirthdayScheduler(BirthdayService birthdayService) {
        this.birthdayService = birthdayService;
    }

    /**
     * Runs internally every night at 12:00:00 AM IST (Asia/Kolkata).
     * Cron expression: second=0, minute=0, hour=0, every day
     */
    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Kolkata")
    public void runMidnightBirthdayWishes() {
        System.out.println("\n⏰ [12:00 AM IST] Midnight reached! Triggering internal birthday job...");
        birthdayService.processDailyBirthdays();
        System.out.println("✨ [12:00 AM IST] Midnight birthday job finished!\n");
    }
}
