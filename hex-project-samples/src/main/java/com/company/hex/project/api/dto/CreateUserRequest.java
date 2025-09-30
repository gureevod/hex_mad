package com.company.hex.project.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for creating a new user.
 * Used with @Body annotation in POST requests.
 * 
 * @author Hex Framework
 * @version 1.0
 */
public class CreateUserRequest {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("username")
    private String username;
    
    // Default constructor for Jackson
    public CreateUserRequest() {
    }
    
    public CreateUserRequest(String name, String email, String username) {
        this.name = name;
        this.email = email;
        this.username = username;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    @Override
    public String toString() {
        return "CreateUserRequest{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}