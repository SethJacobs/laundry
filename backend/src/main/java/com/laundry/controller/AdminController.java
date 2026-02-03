package com.laundry.controller;

import com.laundry.dto.BlockUserRequest;
import com.laundry.model.User;
import com.laundry.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }
    
    @PostMapping("/block-user")
    public ResponseEntity<?> blockUser(@Valid @RequestBody BlockUserRequest request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setBlocked(true);
        user.setBlockedUntil(request.getBlockedUntil());
        user.setBlockReason(request.getReason());
        
        userRepository.save(user);
        return ResponseEntity.ok("User blocked successfully");
    }
    
    @PostMapping("/unblock-user/{userId}")
    public ResponseEntity<?> unblockUser(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setBlocked(false);
        user.setBlockedUntil(null);
        user.setBlockReason(null);
        
        userRepository.save(user);
        return ResponseEntity.ok("User unblocked successfully");
    }
}

