package com.company.hex.project.tests.api;

import com.company.hex.api.service.ApiServiceFactory;
import com.company.hex.project.api.dto.ActivityDto;
import com.company.hex.project.api.dto.CreateActivityRequest;
import com.company.hex.project.api.services.FakeRestApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Demonstrates the declarative API testing approach with FakeRESTApi.
 * This test uses the annotation-driven FakeRestApi interface.
 * 
 * @author Hex Framework
 * @version 1.0
 */
@DisplayName("FakeRESTApi - Activities API Tests")
class FakeRestApiTest {
    
    private FakeRestApi fakeRestApi;
    
    @BeforeEach
    void setUp() {
        // Create the API client using the factory
        fakeRestApi = ApiServiceFactory.create(FakeRestApi.class);
    }
    
    @Test
    @DisplayName("Should get all activities successfully")
    void shouldGetAllActivities() {
        // When: Get all activities
        List<ActivityDto> activities = fakeRestApi.getAllActivities();
        
        // Then: Verify response
        assertThat(activities).isNotNull();
        assertThat(activities).isNotEmpty();
        assertThat(activities.size()).isGreaterThan(0);
        
        // Verify first activity has expected fields
        ActivityDto firstActivity = activities.get(0);
        assertThat(firstActivity.getId()).isPositive();
        assertThat(firstActivity.getTitle()).isNotNull();
        assertThat(firstActivity.getDueDate()).isNotNull();
        
        // Log some details for visibility
        System.out.println("Total activities retrieved: " + activities.size());
        System.out.println("First activity: " + firstActivity);
    }
    
    @Test
    @DisplayName("Should create activity successfully")
    void shouldCreateActivity() {
        // Given: An activity creation request
        CreateActivityRequest request = CreateActivityRequest.builder()
            .id(100)
            .title("Test Activity")
            .dueDate("2025-12-31T23:59:59.000Z")
            .completed(false)
            .build();
        
        // When: Create activity
        ActivityDto createdActivity = fakeRestApi.createActivity(request);
        
        // Then: Verify response
        assertThat(createdActivity).isNotNull();
        assertThat(createdActivity.getId()).isPositive();
        assertThat(createdActivity.getTitle()).isEqualTo("Test Activity");
        assertThat(createdActivity.isCompleted()).isFalse();
        assertThat(createdActivity.getDueDate()).isNotNull();
        
        // Log created activity details
        System.out.println("Created activity: " + createdActivity);
    }
}