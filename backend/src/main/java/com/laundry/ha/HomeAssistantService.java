package com.laundry.ha;

import com.laundry.ha.dto.HAStateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class HomeAssistantService {
    
    private final RestTemplate restTemplate;
    
    @Value("${homeassistant.base-url}")
    private String baseUrl;
    
    @Value("${homeassistant.token:}")
    private String token;
    
    @Value("${homeassistant.enabled:false}")
    private boolean enabled;
    
    @Value("${homeassistant.entity.washer-running}")
    private String washerRunningEntity;
    
    @Value("${homeassistant.entity.washer-time-remaining}")
    private String washerTimeRemainingEntity;
    
    @Value("${homeassistant.entity.washer-status}")
    private String washerStatusEntity;
    
    @Value("${homeassistant.entity.dryer-running}")
    private String dryerRunningEntity;
    
    @Value("${homeassistant.entity.dryer-time-remaining}")
    private String dryerTimeRemainingEntity;
    
    @Value("${homeassistant.entity.dryer-status}")
    private String dryerStatusEntity;
    
    @Value("${homeassistant.entity.washer-sub-cycle:}")
    private String washerSubCycleEntity;
    
    @Value("${homeassistant.entity.washer-end-of-cycle:}")
    private String washerEndOfCycleEntity;
    
    @Value("${homeassistant.entity.dryer-sub-cycle:}")
    private String dryerSubCycleEntity;
    
    @Value("${homeassistant.entity.dryer-end-of-cycle:}")
    private String dryerEndOfCycleEntity;
    
    @Autowired
    public HomeAssistantService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    private HttpEntity<Void> authEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }
    
    private HAStateResponse getEntity(String entityId) {
        if (!enabled || token == null || token.isEmpty()) {
            return null;
        }
        
        try {
            String url = baseUrl + "/api/states/" + entityId;
            ResponseEntity<HAStateResponse> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                authEntity(), 
                HAStateResponse.class
            );
            return response.getBody();
        } catch (RestClientException e) {
            System.err.println("Failed to fetch HA entity " + entityId + ": " + e.getMessage());
            return null;
        }
    }
    
    @Cacheable(value = "washer-status", unless = "#result == null")
    public boolean isWasherRunning() {
        // Check remote status first
        HAStateResponse remoteStatus = getEntity(washerRunningEntity);
        if (remoteStatus != null && "on".equalsIgnoreCase(remoteStatus.getState())) {
            // Also verify machine state to be more accurate
            HAStateResponse machineState = getEntity(washerStatusEntity);
            if (machineState != null && machineState.getState() != null) {
                String state = machineState.getState();
                // Machine is running if state is "Run" or similar
                if ("Run".equalsIgnoreCase(state) || "Running".equalsIgnoreCase(state)) {
                    return true;
                }
                // If state is not "Idle" or "Standby", consider it running
                if (!"Idle".equalsIgnoreCase(state) && !"Standby".equalsIgnoreCase(state)) {
                    return true;
                }
            }
            // If remote status is on, assume running
            return true;
        }
        return false;
    }
    
    @Cacheable(value = "washer-time", unless = "#result == null")
    public Integer getWasherTimeRemaining() {
        HAStateResponse response = getEntity(washerTimeRemainingEntity);
        if (response == null || response.getState() == null) {
            return null;
        }
        try {
            String state = response.getState();
            // Handle "unknown" or empty states
            if (state == null || state.isEmpty() || "unknown".equalsIgnoreCase(state)) {
                return null;
            }
            // Parse decimal minutes and round to nearest integer
            double minutes = Double.parseDouble(state);
            return (int) Math.round(minutes);
        } catch (Exception e) {
            return null;
        }
    }
    
    @Cacheable(value = "washer-status-text", unless = "#result == null")
    public String getWasherStatus() {
        // Check if cycle is finished first
        if (!washerEndOfCycleEntity.isEmpty()) {
            HAStateResponse endOfCycle = getEntity(washerEndOfCycleEntity);
            if (endOfCycle != null && "on".equalsIgnoreCase(endOfCycle.getState())) {
                return "finished";
            }
        }
        
        // Check machine state
        HAStateResponse response = getEntity(washerStatusEntity);
        if (response == null || response.getState() == null) {
            // Fallback to sub-cycle if available
            if (!washerSubCycleEntity.isEmpty()) {
                HAStateResponse subCycle = getEntity(washerSubCycleEntity);
                if (subCycle != null && subCycle.getState() != null) {
                    return subCycle.getState().toLowerCase();
                }
            }
            return "unknown";
        }
        
        String state = response.getState();
        // Normalize common states
        if ("Run".equalsIgnoreCase(state) || "Running".equalsIgnoreCase(state)) {
            return "running";
        }
        if ("Idle".equalsIgnoreCase(state) || "Standby".equalsIgnoreCase(state)) {
            return "idle";
        }
        
        return state != null ? state.toLowerCase() : "unknown";
    }
    
    // Dryer methods
    @Cacheable(value = "dryer-status", unless = "#result == null")
    public boolean isDryerRunning() {
        // Check remote status first
        HAStateResponse remoteStatus = getEntity(dryerRunningEntity);
        if (remoteStatus != null && "on".equalsIgnoreCase(remoteStatus.getState())) {
            // Also verify machine state to be more accurate
            HAStateResponse machineState = getEntity(dryerStatusEntity);
            if (machineState != null && machineState.getState() != null) {
                String state = machineState.getState();
                // Machine is running if state is "Run" or similar
                if ("Run".equalsIgnoreCase(state) || "Running".equalsIgnoreCase(state)) {
                    return true;
                }
                // If state is not "Idle" or "Standby", consider it running
                if (!"Idle".equalsIgnoreCase(state) && !"Standby".equalsIgnoreCase(state)) {
                    return true;
                }
            }
            // If remote status is on, assume running
            return true;
        }
        return false;
    }
    
    @Cacheable(value = "dryer-time", unless = "#result == null")
    public Integer getDryerTimeRemaining() {
        HAStateResponse response = getEntity(dryerTimeRemainingEntity);
        if (response == null || response.getState() == null) {
            return null;
        }
        try {
            String state = response.getState();
            if (state == null || state.isEmpty() || "unknown".equalsIgnoreCase(state)) {
                return null;
            }
            // Parse decimal minutes and round to nearest integer
            double minutes = Double.parseDouble(state);
            return (int) Math.round(minutes);
        } catch (Exception e) {
            return null;
        }
    }
    
    @Cacheable(value = "dryer-status-text", unless = "#result == null")
    public String getDryerStatus() {
        // Check if cycle is finished first
        if (!dryerEndOfCycleEntity.isEmpty()) {
            HAStateResponse endOfCycle = getEntity(dryerEndOfCycleEntity);
            if (endOfCycle != null && "on".equalsIgnoreCase(endOfCycle.getState())) {
                return "finished";
            }
        }
        
        // Check machine state
        HAStateResponse response = getEntity(dryerStatusEntity);
        if (response == null || response.getState() == null) {
            // Fallback to sub-cycle if available
            if (!dryerSubCycleEntity.isEmpty()) {
                HAStateResponse subCycle = getEntity(dryerSubCycleEntity);
                if (subCycle != null && subCycle.getState() != null) {
                    return subCycle.getState().toLowerCase();
                }
            }
            return "unknown";
        }
        
        String state = response.getState();
        // Normalize common states
        if ("Run".equalsIgnoreCase(state) || "Running".equalsIgnoreCase(state)) {
            return "running";
        }
        if ("Idle".equalsIgnoreCase(state) || "Standby".equalsIgnoreCase(state)) {
            return "idle";
        }
        
        return state != null ? state.toLowerCase() : "unknown";
    }
    
    // Evict cache every 30 seconds to keep data fresh
    @Scheduled(fixedRate = 30000)
    @CacheEvict(value = {
        "washer-status", "washer-time", "washer-status-text",
        "dryer-status", "dryer-time", "dryer-status-text"
    }, allEntries = true)
    public void evictMachineCache() {
        // Cache eviction happens automatically
    }
    
    public boolean isEnabled() {
        return enabled && token != null && !token.isEmpty();
    }
}

