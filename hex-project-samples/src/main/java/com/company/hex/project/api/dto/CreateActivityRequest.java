package com.company.hex.project.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new activity.
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
public class CreateActivityRequest {
    
    @JsonProperty("id")
    private int id;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("dueDate")
    private String dueDate;
    
    @JsonProperty("completed")
    private boolean completed;
}