package com.company.hex.project.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new user.
 * Used with @Body annotation in POST requests.
 *
 * <p>Lombok annotations used:
 * <ul>
 *   <li>@Data - generates getters, setters, toString, equals, and hashCode</li>
 *   <li>@Builder - provides builder pattern for fluent object construction</li>
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
public class CreateUserRequest {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("username")
    private String username;
}