package com.laundry.service;

import com.laundry.dto.BookingRequest;
import com.laundry.dto.BookingResponse;
import com.laundry.ha.HomeAssistantService;
import com.laundry.model.Booking;
import com.laundry.model.User;
import com.laundry.repository.BookingRepository;
import com.laundry.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired(required = false)
    private HomeAssistantService haService;
    
    @Transactional
    public BookingResponse createBooking(Long userId, BookingRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.isBlocked()) {
            if (user.getBlockedUntil() != null && LocalDateTime.now().isBefore(user.getBlockedUntil())) {
                throw new RuntimeException("User is blocked until " + user.getBlockedUntil());
            } else if (user.getBlockedUntil() == null) {
                throw new RuntimeException("User is permanently blocked");
            }
        }
        
        
        // Check for overlapping bookings (same time slots, not just same day)
        List<Booking> overlapping = bookingRepository.findOverlappingBookings(
            request.getStartTime(), 
            request.getEndTime()
        );
        
        if (!overlapping.isEmpty()) {
            throw new RuntimeException("Time slot overlaps with existing booking");
        }
        
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setNotes(request.getNotes());
        
        booking = bookingRepository.save(booking);
        return convertToResponse(booking);
    }
    
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookings(LocalDateTime start, LocalDateTime end) {
        List<Booking> bookings = bookingRepository.findBookingsInRange(start, end);
        return bookings.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<BookingResponse> getUserBookings(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        return user.getBookings().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public void deleteBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (!booking.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to delete this booking");
        }
        
        bookingRepository.delete(booking);
    }
    
    @Transactional(readOnly = true)
    public List<Booking> getTodayBookings() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1).withHour(0).withMinute(0).withSecond(0);
        return bookingRepository.findTodayBookings(now, tomorrow);
    }
    
    @Transactional(readOnly = true)
    public List<Booking> getWeekBookings() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekStart = now.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime weekEnd = weekStart.plusWeeks(1);
        return bookingRepository.findWeekBookings(weekStart, weekEnd);
    }
    
    @Transactional
    public BookingResponse bookNextAvailable(Long userId, int durationMinutes, String notes) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.isBlocked()) {
            if (user.getBlockedUntil() != null && LocalDateTime.now().isBefore(user.getBlockedUntil())) {
                throw new RuntimeException("User is blocked until " + user.getBlockedUntil());
            } else if (user.getBlockedUntil() == null) {
                throw new RuntimeException("User is permanently blocked");
            }
        }
        
        LocalDateTime nextSlot = findNextAvailableSlot(durationMinutes);
        if (nextSlot == null) {
            throw new RuntimeException("No available slots found in the next 7 days");
        }
        
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setStartTime(nextSlot);
        booking.setEndTime(nextSlot.plusMinutes(durationMinutes));
        booking.setNotes(notes);
        
        booking = bookingRepository.save(booking);
        return convertToResponse(booking);
    }
    
    private LocalDateTime findNextAvailableSlot(int durationMinutes) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime searchStart = now.plusMinutes(30); // Start searching 30 minutes from now
        LocalDateTime searchEnd = now.plusDays(7); // Search up to 7 days ahead
        
        // Round to next hour for cleaner slots
        searchStart = searchStart.withMinute(0).withSecond(0).withNano(0);
        if (searchStart.isBefore(now.plusMinutes(30))) {
            searchStart = searchStart.plusHours(1);
        }
        
        // Operating hours: 6 AM to 11 PM
        int startHour = 6;
        int endHour = 23;
        
        LocalDateTime current = searchStart;
        while (current.isBefore(searchEnd)) {
            // Skip if outside operating hours
            if (current.getHour() < startHour || current.getHour() >= endHour) {
                current = current.plusHours(1);
                continue;
            }
            
            LocalDateTime slotEnd = current.plusMinutes(durationMinutes);
            
            // Check if this slot would extend past operating hours
            if (slotEnd.getHour() > endHour || (slotEnd.getHour() == endHour && slotEnd.getMinute() > 0)) {
                // Move to next day at start hour
                current = current.plusDays(1).withHour(startHour).withMinute(0).withSecond(0).withNano(0);
                continue;
            }
            
            // Check for overlapping bookings
            List<Booking> overlapping = bookingRepository.findOverlappingBookings(current, slotEnd);
            
            if (overlapping.isEmpty()) {
                return current;
            }
            
            // Move to next hour
            current = current.plusHours(1);
        }
        
        return null; // No available slot found
    }
    
    private BookingResponse convertToResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setUserId(booking.getUser().getId());
        response.setUsername(booking.getUser().getUsername());
        response.setFirstName(booking.getUser().getFirstName());
        response.setLastName(booking.getUser().getLastName());
        response.setStartTime(booking.getStartTime());
        response.setEndTime(booking.getEndTime());
        response.setNotes(booking.getNotes());
        response.setCreatedAt(booking.getCreatedAt());
        return response;
    }
}

