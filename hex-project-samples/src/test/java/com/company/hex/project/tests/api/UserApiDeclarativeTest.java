package com.company.hex.project.tests.api;

import com.company.hex.api.service.ApiServiceFactory;
import com.company.hex.project.api.dto.CreateUserRequest;
import com.company.hex.project.api.dto.UserDto;
import com.company.hex.project.api.services.UserApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Demonstrates the declarative API testing approach.
 * This test uses the new annotation-driven UserApi interface.
 * 
 * @author Hex Framework
 * @version 1.0
 */
@DisplayName("User API - Declarative Approach Tests")
class UserApiDeclarativeTest {
    
    private UserApi userApi;
    
    @BeforeEach
    void setUp() {
        // Create the API client using the factory
        userApi = ApiServiceFactory.create(UserApi.class);
    }
    
    @Test
    @DisplayName("Should get all users successfully")
    void shouldGetAllUsers() {
        // When: Get all users
        List<UserDto> users = userApi.getAllUsers();
        
        // Then: Verify response
        assertThat(users).isNotNull();
        assertThat(users).isNotEmpty();
        assertThat(users.size()).isGreaterThan(0);
        
        // Verify first user has expected fields
        UserDto firstUser = users.get(0);
        assertThat(firstUser.getId()).isPositive();
        assertThat(firstUser.getName()).isNotEmpty();
        assertThat(firstUser.getEmail()).isNotEmpty();
    }
    
    @Test
    @DisplayName("Should get user by ID successfully")
    void shouldGetUserById() {
        // Given: A valid user ID
        int userId = 1;
        
        // When: Get user by ID
        UserDto user = userApi.getUserById(userId);
        
        // Then: Verify response
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(userId);
        assertThat(user.getName()).isNotEmpty();
        assertThat(user.getEmail()).isNotEmpty();
        assertThat(user.getUsername()).isNotEmpty();
    }
    
    @Test
    @DisplayName("Should search users by username")
    void shouldSearchUsersByUsername() {
        // Given: A username to search for
        String username = "Bret";
        
        // When: Search users by username
        List<UserDto> users = userApi.searchUsersByUsername(username);
        
        // Then: Verify response
        assertThat(users).isNotNull();
        assertThat(users).isNotEmpty();
        assertThat(users.get(0).getUsername()).isEqualTo(username);
    }
    
    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUser() {
        // Given: A user creation request
        CreateUserRequest request = new CreateUserRequest(
            "John Doe",
            "john.doe@example.com",
            "johndoe"
        );
        
        // When: Create user
        UserDto createdUser = userApi.createUser(request);
        
        // Then: Verify response
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isPositive();
        // Note: JSONPlaceholder returns id 11 for POST requests
    }
    
    @Test
    @DisplayName("Should handle multiple sequential requests")
    void shouldHandleMultipleRequests() {
        // When: Make multiple requests
        UserDto user1 = userApi.getUserById(1);
        UserDto user2 = userApi.getUserById(2);
        List<UserDto> allUsers = userApi.getAllUsers();
        
        // Then: All requests should succeed
        assertThat(user1).isNotNull();
        assertThat(user2).isNotNull();
        assertThat(allUsers).isNotEmpty();
        
        assertThat(user1.getId()).isEqualTo(1);
        assertThat(user2.getId()).isEqualTo(2);
    }
}