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
        
        
        // Check for overlapping bookings
        List<Booking> overlapping = bookingRepository.findOverlappingBookings(
            request.getStartTime(), 
            request.getEndTime()
        );
        
        if (!overlapping.isEmpty()) {
            throw new RuntimeException("Time slot is already booked");
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

