package com.laundry.controller;

import com.laundry.ha.HomeAssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/machines")
@CrossOrigin(origins = "*")
public class MachineController {
    
    @Autowired
    private HomeAssistantService haService;
    
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        System.out.println("[MachineController] TEST endpoint called");
        return ResponseEntity.ok(Map.of("status", "working", "message", "Test endpoint is working"));
    }

    @GetMapping("/washer")
    public ResponseEntity<?> getWasherStatus() {
        System.out.println("[MachineController] GET /machines/washer called");
        try {
            if (!haService.isEnabled()) {
                System.out.println("[MachineController] Home Assistant is not enabled");
                return ResponseEntity.ok(Map.of(
                    "enabled", false,
                    "status", "unknown",
                    "running", false,
                    "timeRemainingMinutes", null
                ));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("enabled", true);
            response.put("status", haService.getWasherStatus());
            response.put("running", haService.isWasherRunning());
            response.put("timeRemainingMinutes", haService.getWasherTimeRemaining());
            
            System.out.println("[MachineController] Washer response: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("[MachineController] Error getting washer status: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                "enabled", false,
                "status", "error",
                "running", false,
                "timeRemainingMinutes", null,
                "error", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/dryer")
    public ResponseEntity<?> getDryerStatus() {
        System.out.println("[MachineController] GET /machines/dryer called");
        try {
            if (!haService.isEnabled()) {
                System.out.println("[MachineController] Home Assistant is not enabled");
                return ResponseEntity.ok(Map.of(
                    "enabled", false,
                    "status", "unknown",
                    "running", false,
                    "timeRemainingMinutes", null
                ));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("enabled", true);
            response.put("status", haService.getDryerStatus());
            response.put("running", haService.isDryerRunning());
            response.put("timeRemainingMinutes", haService.getDryerTimeRemaining());
            
            System.out.println("[MachineController] Dryer response: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("[MachineController] Error getting dryer status: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                "enabled", false,
                "status", "error",
                "running", false,
                "timeRemainingMinutes", null,
                "error", e.getMessage()
            ));
        }
    }
}

