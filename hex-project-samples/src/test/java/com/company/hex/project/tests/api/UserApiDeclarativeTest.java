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
    
    @Test
    @DisplayName("Should update user successfully using PUT")
    void shouldUpdateUser() {
        // Given: A user ID and update request
        int userId = 1;
        CreateUserRequest updateRequest = new CreateUserRequest(
            "Jane Doe Updated",
            "jane.updated@example.com",
            "janeupdated"
        );
        
        // When: Update user
        UserDto updatedUser = userApi.updateUser(userId, updateRequest);
        
        // Then: Verify response
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getId()).isEqualTo(userId);
        // Note: JSONPlaceholder simulates the update but doesn't persist changes
    }
    
    @Test
    @DisplayName("Should delete user successfully with status validation")
    void shouldDeleteUser() {
        // Given: A user ID to delete
        int userId = 1;
        
        // When: Delete user
        // The @ExpectedStatus(200) annotation will validate the response status
        userApi.deleteUser(userId);
        
        // Then: No exception means the status was 200 as expected
        // JSONPlaceholder returns 200 for DELETE operations
    }
    
    @Test
    @DisplayName("Should get user with custom authorization header")
    void shouldGetUserWithAuthHeader() {
        // Given: A user ID and auth token
        int userId = 1;
        String authToken = "Bearer test-token-12345";
        
        // When: Get user with authorization header
        UserDto user = userApi.getUserWithAuth(userId, authToken);
        
        // Then: Verify response
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(userId);
        assertThat(user.getName()).isNotEmpty();
        // Note: JSONPlaceholder doesn't validate auth, but the header is sent
    }
    
    @Test
    @DisplayName("Should demonstrate all CRUD operations")
    void shouldDemonstrateFullCrudCycle() {
        // Create
        CreateUserRequest createRequest = new CreateUserRequest(
            "Test User",
            "test@example.com",
            "testuser"
        );
        UserDto created = userApi.createUser(createRequest);
        assertThat(created).isNotNull();
        
        // Read
        UserDto retrieved = userApi.getUserById(1);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getId()).isEqualTo(1);
        
        // Update
        CreateUserRequest updateRequest = new CreateUserRequest(
            "Updated User",
            "updated@example.com",
            "updateduser"
        );
        UserDto updated = userApi.updateUser(1, updateRequest);
        assertThat(updated).isNotNull();
        
        // Delete
        userApi.deleteUser(1);
        // No exception means successful deletion with expected status
    }
}