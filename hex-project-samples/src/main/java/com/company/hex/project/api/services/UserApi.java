package com.company.hex.project.api.services;

import com.company.hex.api.annotations.config.ApiService;
import com.company.hex.api.annotations.config.ExpectedStatus;
import com.company.hex.api.annotations.http.DELETE;
import com.company.hex.api.annotations.http.GET;
import com.company.hex.api.annotations.http.POST;
import com.company.hex.api.annotations.http.PUT;
import com.company.hex.api.annotations.param.Body;
import com.company.hex.api.annotations.param.Header;
import com.company.hex.api.annotations.param.Path;
import com.company.hex.api.annotations.param.Query;
import com.company.hex.project.api.dto.CreateUserRequest;
import com.company.hex.project.api.dto.UserDto;

import java.util.List;

/**
 * Declarative API interface for User operations.
 * Demonstrates the new annotation-driven approach for API testing.
 * 
 * <p>Usage example:
 * <pre>
 * {@code
 * UserApi userApi = ApiServiceFactory.create(UserApi.class);
 * UserDto user = userApi.getUserById(1);
 * List<UserDto> users = userApi.getAllUsers();
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 */
@ApiService(baseUrl = "https://jsonplaceholder.typicode.com")
public interface UserApi {
    
    /**
     * Get all users.
     * 
     * @return list of all users
     */
    @GET("/users")
    List<UserDto> getAllUsers();
    
    /**
     * Get a specific user by ID.
     * 
     * @param userId the user ID
     * @return the user details
     */
    @GET("/users/{id}")
    UserDto getUserById(@Path("id") int userId);
    
    /**
     * Search users by name.
     * Demonstrates query parameter usage.
     * 
     * @param name the name to search for
     * @return list of matching users
     */
    @GET("/users")
    List<UserDto> searchUsersByName(@Query("name") String name);
    
    /**
     * Search users by username.
     * 
     * @param username the username to search for
     * @return list of matching users
     */
    @GET("/users")
    List<UserDto> searchUsersByUsername(@Query("username") String username);
    
    /**
     * Create a new user.
     * Demonstrates POST with request body.
     * 
     * @param request the user creation request
     * @return the created user
     */
    @POST("/users")
    UserDto createUser(@Body CreateUserRequest request);
    
    /**
     * Update an existing user.
     * Demonstrates PUT with path parameter and request body.
     *
     * @param userId the user ID to update
     * @param request the user update request
     * @return the updated user
     */
    @PUT("/users/{id}")
    UserDto updateUser(@Path("id") int userId, @Body CreateUserRequest request);
    
    /**
     * Delete a user by ID.
     * Demonstrates DELETE with expected status validation.
     *
     * @param userId the user ID to delete
     */
    @DELETE("/users/{id}")
    @ExpectedStatus(200)
    void deleteUser(@Path("id") int userId);
    
    /**
     * Get user with custom authorization header.
     * Demonstrates @Header annotation usage.
     *
     * @param userId the user ID
     * @param authToken the authorization token
     * @return the user details
     */
    @GET("/users/{id}")
    UserDto getUserWithAuth(@Path("id") int userId, @Header("Authorization") String authToken);
}