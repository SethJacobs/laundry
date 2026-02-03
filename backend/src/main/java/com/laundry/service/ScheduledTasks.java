package com.laundry.service;

import com.laundry.model.Booking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScheduledTasks {
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private EmailService emailService;
    
    // Send weekly schedule every Monday at 8 AM
    @Scheduled(cron = "0 0 8 * * MON")
    public void sendWeeklySchedule() {
        List<Booking> weekBookings = bookingService.getWeekBookings();
        emailService.sendWeeklySchedule(weekBookings);
    }
    
    // Send daily reminders every day at 7 AM
    @Scheduled(cron = "0 0 7 * * *")
    public void sendDailyReminders() {
        List<Booking> todayBookings = bookingService.getTodayBookings();
        todayBookings.forEach(emailService::sendDailyReminder);
    }
}

