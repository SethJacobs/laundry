package com.laundry.ha.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HAStateResponse {
    private String entity_id;
    private String state;
    private Map<String, Object> attributes;
    
    public String getEntity_id() {
        return entity_id;
    }
    
    public void setEntity_id(String entity_id) {
        this.entity_id = entity_id;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}

