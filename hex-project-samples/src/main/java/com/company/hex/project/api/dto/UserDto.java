package com.company.hex.project.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for User entity from JSONPlaceholder API.
 * Demonstrates declarative API usage with type-safe responses.
 * Uses @JsonIgnoreProperties to ignore nested objects (address, company) for MVP simplicity.
 *
 * <p>Lombok annotations used:
 * <ul>
 *   <li>@Data - generates getters, setters, toString, equals, and hashCode</li>
 *   <li>@Builder - provides builder pattern for object construction</li>
 *   <li>@NoArgsConstructor - generates default constructor for Jackson</li>
 *   <li>@AllArgsConstructor - generates constructor with all fields for builder</li>
 * </ul>
 *
 * @author Hex Framework
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {
    
    @JsonProperty("id")
    private int id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("phone")
    private String phone;
    
    @JsonProperty("website")
    private String website;
}