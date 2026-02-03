package com.laundry.service;

import com.laundry.model.Booking;
import com.laundry.model.User;
import com.laundry.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private UserRepository userRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");
    
    public void sendWeeklySchedule(List<Booking> bookings) {
        List<User> users = userRepository.findAll();
        
        for (User user : users) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Weekly Laundry Schedule");
            message.setText(buildWeeklyScheduleEmail(bookings));
            
            try {
                mailSender.send(message);
            } catch (Exception e) {
                System.err.println("Failed to send email to " + user.getEmail() + ": " + e.getMessage());
            }
        }
    }
    
    public void sendDailyReminder(Booking booking) {
        User user = booking.getUser();
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Laundry Reminder: Your booking is today!");
        message.setText(buildDailyReminderEmail(booking));
        
        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send reminder to " + user.getEmail() + ": " + e.getMessage());
        }
    }
    
    private String buildWeeklyScheduleEmail(List<Booking> bookings) {
        StringBuilder email = new StringBuilder();
        email.append("Hello!\n\n");
        email.append("Here's the laundry schedule for the upcoming week:\n\n");
        
        if (bookings.isEmpty()) {
            email.append("No bookings scheduled for this week.\n");
        } else {
            bookings.forEach(booking -> {
                email.append(String.format("%s - %s: %s %s\n",
                    booking.getStartTime().format(DATE_FORMATTER),
                    booking.getStartTime().format(TIME_FORMATTER),
                    booking.getUser().getFirstName() != null ? booking.getUser().getFirstName() : booking.getUser().getUsername(),
                    booking.getUser().getLastName() != null ? booking.getUser().getLastName() : ""
                ));
                if (booking.getNotes() != null && !booking.getNotes().isEmpty()) {
                    email.append("  Notes: ").append(booking.getNotes()).append("\n");
                }
                email.append("\n");
            });
        }
        
        email.append("\nHave a great week!\n");
        return email.toString();
    }
    
    private String buildDailyReminderEmail(Booking booking) {
        StringBuilder email = new StringBuilder();
        email.append("Hello ").append(
            booking.getUser().getFirstName() != null ? 
            booking.getUser().getFirstName() : 
            booking.getUser().getUsername()
        ).append("!\n\n");
        email.append("This is a reminder that you have a laundry booking today:\n\n");
        email.append(String.format("Time: %s - %s\n",
            booking.getStartTime().format(TIME_FORMATTER),
            booking.getEndTime().format(TIME_FORMATTER)
        ));
        if (booking.getNotes() != null && !booking.getNotes().isEmpty()) {
            email.append("Notes: ").append(booking.getNotes()).append("\n");
        }
        email.append("\nDon't forget!\n");
        return email.toString();
    }
}

