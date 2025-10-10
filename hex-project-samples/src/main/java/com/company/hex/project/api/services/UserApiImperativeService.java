package com.company.hex.project.api.services;

import com.company.hex.api.config.ApiConfig;
import com.company.hex.api.service.BaseApiService;
import com.company.hex.project.api.dto.CreateUserRequest;
import com.company.hex.project.api.dto.UserDto;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.List;
import java.util.Map;

/**
 * Pure imperative API service for User operations.
 * Demonstrates direct RestAssured usage while maintaining framework benefits.
 * 
 * <p>This approach provides full control over request building and is ideal for:
 * <ul>
 *   <li>Complex query parameter construction</li>
 *   <li>Dynamic request building based on runtime conditions</li>
 *   <li>Custom filters and interceptors</li>
 *   <li>Non-standard HTTP operations</li>
 * </ul>
 * 
 * <p>Usage example:
 * <pre>
 * {@code
 * UserApiImperativeService userService = ApiServiceFactory.createLegacy(UserApiImperativeService.class);
 * UserDto user = userService.getUserById(1);
 * List<UserDto> users = userService.searchUsersWithComplexFilters(filters);
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 */
public class UserApiImperativeService extends BaseApiService {

    /**
     * Default constructor using default configuration.
     */
    public UserApiImperativeService() {
        super();
    }

    /**
     * Constructor with custom configuration.
     * 
     * @param config custom API configuration
     */
    public UserApiImperativeService(ApiConfig config) {
        super(config);
    }

    /**
     * Get all users using imperative approach.
     * 
     * @return list of all users
     */
    public List<UserDto> getAllUsers() {
        logger.info("Getting all users (imperative)");
        
        return newRequest()
            .when()
            .get("/users")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getList(".", UserDto.class);
    }

    /**
     * Get a specific user by ID.
     * 
     * @param userId the user ID
     * @return the user details
     */
    public UserDto getUserById(int userId) {
        logger.info("Getting user by ID: {} (imperative)", userId);
        
        return newRequest()
            .pathParam("id", userId)
            .when()
            .get("/users/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(UserDto.class);
    }

    /**
     * Search users by name using simple query parameter.
     * 
     * @param name the name to search for
     * @return list of matching users
     */
    public List<UserDto> searchUsersByName(String name) {
        logger.info("Searching users by name: {} (imperative)", name);
        
        return newRequest()
            .queryParam("name", name)
            .when()
            .get("/users")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getList(".", UserDto.class);
    }

    /**
     * Search users with complex dynamic filters.
     * Demonstrates the power of imperative approach for dynamic query building.
     * 
     * @param filters map of filter criteria (key-value pairs)
     * @return list of matching users
     */
    public List<UserDto> searchUsersWithComplexFilters(Map<String, Object> filters) {
        logger.info("Searching users with complex filters: {} (imperative)", filters);
        
        RequestSpecification request = newRequest();
        
        // Dynamically build query parameters based on provided filters
        filters.forEach((key, value) -> {
            if (value instanceof List) {
                // Handle list values (e.g., multiple IDs)
                request.queryParam(key, ((List<?>) value).toArray());
            } else if (value != null) {
                // Handle single values
                request.queryParam(key, value);
            }
        });
        
        return request
            .when()
            .get("/users")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getList(".", UserDto.class);
    }

    /**
     * Create a new user.
     * 
     * @param request the user creation request
     * @return the created user
     */
    public UserDto createUser(CreateUserRequest request) {
        logger.info("Creating user: {} (imperative)", request.getName());
        
        return newRequest()
            .body(request)
            .when()
            .post("/users")
            .then()
            .statusCode(201)
            .extract()
            .as(UserDto.class);
    }

    /**
     * Update an existing user.
     * 
     * @param userId the user ID to update
     * @param request the user update request
     * @return the updated user
     */
    public UserDto updateUser(int userId, CreateUserRequest request) {
        logger.info("Updating user ID: {} (imperative)", userId);
        
        return newRequest()
            .pathParam("id", userId)
            .body(request)
            .when()
            .put("/users/{id}")
            .then()
            .statusCode(200)
            .extract()
            .as(UserDto.class);
    }

    /**
     * Delete a user by ID.
     * 
     * @param userId the user ID to delete
     * @return the response
     */
    public Response deleteUser(int userId) {
        logger.info("Deleting user ID: {} (imperative)", userId);
        
        return newRequest()
            .pathParam("id", userId)
            .when()
            .delete("/users/{id}")
            .then()
            .statusCode(200)
            .extract()
            .response();
    }

    /**
     * Batch update users with conditional logic.
     * Demonstrates complex imperative logic that would be difficult with pure declarative approach.
     * 
     * @param userIds list of user IDs to update
     * @param updates map of field updates
     * @return number of successfully updated users
     */
    public int batchUpdateUsers(List<Integer> userIds, Map<String, Object> updates) {
        logger.info("Batch updating {} users (imperative)", userIds.size());
        
        int successCount = 0;
        
        for (Integer userId : userIds) {
            try {
                CreateUserRequest updateRequest = CreateUserRequest.builder()
                    .name((String) updates.getOrDefault("name", ""))
                    .email((String) updates.getOrDefault("email", ""))
                    .username((String) updates.getOrDefault("username", ""))
                    .build();
                
                Response response = newRequest()
                    .pathParam("id", userId)
                    .body(updateRequest)
                    .when()
                    .put("/users/{id}");
                
                if (response.getStatusCode() == 200) {
                    successCount++;
                    logger.debug("Successfully updated user ID: {}", userId);
                } else {
                    logger.warn("Failed to update user ID: {} - Status: {}", userId, response.getStatusCode());
                }
            } catch (Exception e) {
                logger.error("Error updating user ID: {}", userId, e);
            }
        }
        
        logger.info("Batch update completed: {}/{} users updated successfully", successCount, userIds.size());
        return successCount;
    }

    /**
     * Get user with custom headers and validation.
     * Demonstrates full control over request configuration.
     * 
     * @param userId the user ID
     * @param authToken custom authorization token
     * @param validateResponse whether to perform additional response validation
     * @return the user details
     */
    public UserDto getUserWithCustomHeaders(int userId, String authToken, boolean validateResponse) {
        logger.info("Getting user ID: {} with custom headers (imperative)", userId);
        
        RequestSpecification request = newRequest()
            .pathParam("id", userId)
            .header("Authorization", "Bearer " + authToken)
            .header("X-Custom-Header", "imperative-request");
        
        Response response = request
            .when()
            .get("/users/{id}");
        
        if (validateResponse) {
            response.then()
                .statusCode(200)
                .contentType("application/json");
        }
        
        return response.as(UserDto.class);
    }

    /**
     * Execute a raw request with full RestAssured control.
     * This is the ultimate "escape hatch" for complex scenarios.
     * 
     * @param userId the user ID
     * @return raw Response object for custom processing
     */
    public Response executeRawRequest(int userId) {
        logger.info("Executing raw request for user ID: {} (imperative)", userId);
        
        return getRequestSpecification()
            .pathParam("id", userId)
            .log().all()  // Full request logging
            .when()
            .get("/users/{id}")
            .then()
            .log().all()  // Full response logging
            .extract()
            .response();
    }
}