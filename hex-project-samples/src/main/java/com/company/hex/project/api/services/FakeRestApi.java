package com.company.hex.project.api.services;

import com.company.hex.api.annotations.config.ApiService;
import com.company.hex.api.annotations.config.ExpectedStatus;
import com.company.hex.api.annotations.http.GET;
import com.company.hex.api.annotations.http.POST;
import com.company.hex.api.annotations.param.Body;
import com.company.hex.project.api.dto.ActivityDto;
import com.company.hex.project.api.dto.CreateActivityRequest;

import java.util.List;

/**
 * Declarative API interface for FakeRESTApi Activity operations.
 * Demonstrates the annotation-driven approach for API testing with FakeRESTApi.
 * 
 * <p>This service provides access to the Activities endpoints from FakeRESTApi,
 * including retrieving all activities and creating new activities.
 * 
 * <p>Usage example:
 * <pre>
 * {@code
 * FakeRestApi fakeRestApi = ApiServiceFactory.create(FakeRestApi.class);
 * List<ActivityDto> activities = fakeRestApi.getAllActivities();
 * ActivityDto newActivity = fakeRestApi.createActivity(request);
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 */
@ApiService(baseUrl = "https://fakerestapi.azurewebsites.net")
public interface FakeRestApi {
    
    /**
     * Get all activities from the FakeRESTApi.
     * 
     * <p>Endpoint: GET /api/v1/Activities
     * <p>Returns a list of all available activities with their details including
     * id, title, due date, and completion status.
     * 
     * @return list of all activities
     */
    @GET("/api/v1/Activities")
    @ExpectedStatus(200)
    List<ActivityDto> getAllActivities();
    
    /**
     * Create a new activity in the FakeRESTApi.
     * Demonstrates POST with request body.
     * 
     * <p>Endpoint: POST /api/v1/Activities
     * <p>Creates a new activity with the provided details and returns
     * the created activity object with all fields populated.
     * 
     * @param request the activity creation request containing id, title, dueDate, and completed status
     * @return the created activity with all details
     */
    @POST("/api/v1/Activities")
    @ExpectedStatus(200)
    ActivityDto createActivity(@Body CreateActivityRequest request);
}