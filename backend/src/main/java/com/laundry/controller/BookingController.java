package com.laundry.controller;

import com.laundry.dto.BookingRequest;
import com.laundry.dto.BookingResponse;
import com.laundry.security.UserPrincipal;
import com.laundry.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/bookings")
@CrossOrigin(origins = "*")
public class BookingController {
    
    @Autowired
    private BookingService bookingService;
    
    @PostMapping
    public ResponseEntity<?> createBooking(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody BookingRequest request) {
        try {
            BookingResponse booking = bookingService.createBooking(userPrincipal.getId(), request);
            return ResponseEntity.ok(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping
    public ResponseEntity<List<BookingResponse>> getBookings(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        if (start == null) {
            start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        }
        if (end == null) {
            end = start.plusMonths(1);
        }
        
        List<BookingResponse> bookings = bookingService.getBookings(start, end);
        return ResponseEntity.ok(bookings);
    }
    
    @GetMapping("/my-bookings")
    public ResponseEntity<List<BookingResponse>> getMyBookings(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<BookingResponse> bookings = bookingService.getUserBookings(userPrincipal.getId());
        return ResponseEntity.ok(bookings);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBooking(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        try {
            bookingService.deleteBooking(id, userPrincipal.getId());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/next-available")
    public ResponseEntity<?> bookNextAvailable(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "120") int durationMinutes,
            @RequestParam(required = false) String notes) {
        try {
            BookingResponse booking = bookingService.bookNextAvailable(userPrincipal.getId(), durationMinutes, notes);
            return ResponseEntity.ok(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

