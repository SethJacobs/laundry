package com.laundry.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class BlockUserRequest {
    @NotNull
    private Long userId;
    
    private LocalDateTime blockedUntil;
    
    private String reason;
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public LocalDateTime getBlockedUntil() {
        return blockedUntil;
    }
    
    public void setBlockedUntil(LocalDateTime blockedUntil) {
        this.blockedUntil = blockedUntil;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
}

