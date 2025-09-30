package com.company.hex.project.api.services;

import com.company.hex.api.config.ApiConfig;
import com.company.hex.api.service.ApiServiceFactory;
import com.company.hex.api.service.BaseApiService;
import com.company.hex.project.api.dto.CreateUserRequest;
import com.company.hex.project.api.dto.UserDto;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.List;
import java.util.Map;

/**
 * Hybrid API service combining declarative and imperative approaches.
 * Demonstrates the flexibility of mixing both styles based on use case complexity.
 * 
 * <p>This service:
 * <ul>
 *   <li>Delegates simple CRUD operations to declarative interface</li>
 *   <li>Implements complex operations imperatively for full control</li>
 *   <li>Provides the best of both worlds</li>
 * </ul>
 * 
 * <p>Usage example:
 * <pre>
 * {@code
 * UserApiHybridService userService = new UserApiHybridService();
 * 
 * // Simple operation - uses declarative approach internally
 * UserDto user = userService.getUserById(1);
 * 
 * // Complex operation - uses imperative approach
 * List<UserDto> users = userService.searchUsersWithComplexFilters(filters);
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 */
public class UserApiHybridService extends BaseApiService implements UserApi {

    private final UserApi declarativeApi;

    /**
     * Default constructor using default configuration.
     * Creates internal declarative API instance for delegation.
     */
    public UserApiHybridService() {
        super();
        this.declarativeApi = ApiServiceFactory.create(UserApi.class);
        logger.info("Initialized hybrid service with declarative delegation");
    }

    /**
     * Constructor with custom configuration.
     * 
     * @param config custom API configuration
     */
    public UserApiHybridService(ApiConfig config) {
        super(config);
        this.declarativeApi = ApiServiceFactory.create(UserApi.class, config);
        logger.info("Initialized hybrid service with custom config and declarative delegation");
    }

    // ========== Declarative Delegations (Simple Operations) ==========

    /**
     * Get all users - delegates to declarative implementation.
     * Simple operation that doesn't need imperative control.
     */
    @Override
    public List<UserDto> getAllUsers() {
        logger.debug("Delegating getAllUsers to declarative API");
        return declarativeApi.getAllUsers();
    }

    /**
     * Get user by ID - delegates to declarative implementation.
     * Simple operation that doesn't need imperative control.
     */
    @Override
    public UserDto getUserById(int userId) {
        logger.debug("Delegating getUserById to declarative API");
        return declarativeApi.getUserById(userId);
    }

    /**
     * Search users by name - delegates to declarative implementation.
     * Simple query parameter operation.
     */
    @Override
    public List<UserDto> searchUsersByName(String name) {
        logger.debug("Delegating searchUsersByName to declarative API");
        return declarativeApi.searchUsersByName(name);
    }

    /**
     * Search users by username - delegates to declarative implementation.
     * Simple query parameter operation.
     */
    @Override
    public List<UserDto> searchUsersByUsername(String username) {
        logger.debug("Delegating searchUsersByUsername to declarative API");
        return declarativeApi.searchUsersByUsername(username);
    }

    /**
     * Create user - delegates to declarative implementation.
     * Standard POST operation.
     */
    @Override
    public UserDto createUser(CreateUserRequest request) {
        logger.debug("Delegating createUser to declarative API");
        return declarativeApi.createUser(request);
    }

    /**
     * Update user - delegates to declarative implementation.
     * Standard PUT operation.
     */
    @Override
    public UserDto updateUser(int userId, CreateUserRequest request) {
        logger.debug("Delegating updateUser to declarative API");
        return declarativeApi.updateUser(userId, request);
    }

    /**
     * Delete user - delegates to declarative implementation.
     * Standard DELETE operation.
     */
    @Override
    public void deleteUser(int userId) {
        logger.debug("Delegating deleteUser to declarative API");
        declarativeApi.deleteUser(userId);
    }

    /**
     * Get user with auth - delegates to declarative implementation.
     * Simple header parameter operation.
     */
    @Override
    public UserDto getUserWithAuth(int userId, String authToken) {
        logger.debug("Delegating getUserWithAuth to declarative API");
        return declarativeApi.getUserWithAuth(userId, authToken);
    }

    // ========== Imperative Implementations (Complex Operations) ==========

    /**
     * Search users with complex dynamic filters.
     * Uses imperative approach for dynamic query building.
     * 
     * @param filters map of filter criteria (key-value pairs)
     * @return list of matching users
     */
    public List<UserDto> searchUsersWithComplexFilters(Map<String, Object> filters) {
        logger.info("Searching users with complex filters (imperative): {}", filters);
        
        RequestSpecification request = newRequest();
        
        // Dynamically build query parameters based on provided filters
        filters.forEach((key, value) -> {
            if (value instanceof List) {
                // Handle list values (e.g., multiple IDs)
                request.queryParam(key, ((List<?>) value).toArray());
                logger.debug("Added array query param: {} = {}", key, value);
            } else if (value != null) {
                // Handle single values
                request.queryParam(key, value);
                logger.debug("Added query param: {} = {}", key, value);
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
     * Batch update users with conditional logic and error handling.
     * Uses imperative approach for complex batch operations.
     * 
     * @param userIds list of user IDs to update
     * @param updates map of field updates
     * @return number of successfully updated users
     */
    public int batchUpdateUsers(List<Integer> userIds, Map<String, Object> updates) {
        logger.info("Batch updating {} users (imperative)", userIds.size());
        
        int successCount = 0;
        int failureCount = 0;
        
        for (Integer userId : userIds) {
            try {
                CreateUserRequest updateRequest = new CreateUserRequest(
                    (String) updates.getOrDefault("name", ""),
                    (String) updates.getOrDefault("email", ""),
                    (String) updates.getOrDefault("username", "")
                );
                
                Response response = newRequest()
                    .pathParam("id", userId)
                    .body(updateRequest)
                    .when()
                    .put("/users/{id}");
                
                if (response.getStatusCode() == 200) {
                    successCount++;
                    logger.debug("Successfully updated user ID: {}", userId);
                } else {
                    failureCount++;
                    logger.warn("Failed to update user ID: {} - Status: {}", userId, response.getStatusCode());
                }
            } catch (Exception e) {
                failureCount++;
                logger.error("Error updating user ID: {}", userId, e);
            }
        }
        
        logger.info("Batch update completed: {}/{} users updated successfully, {} failures", 
                   successCount, userIds.size(), failureCount);
        return successCount;
    }

    /**
     * Get user with custom validation and retry logic.
     * Uses imperative approach for complex validation scenarios.
     * 
     * @param userId the user ID
     * @param validateEmail whether to validate email format in response
     * @param retryOnFailure whether to retry on failure
     * @return the user details
     */
    public UserDto getUserWithValidation(int userId, boolean validateEmail, boolean retryOnFailure) {
        logger.info("Getting user ID: {} with validation (imperative)", userId);
        
        if (retryOnFailure) {
            return executeWithRetry(() -> fetchAndValidateUser(userId, validateEmail));
        } else {
            return fetchAndValidateUser(userId, validateEmail);
        }
    }

    /**
     * Internal method to fetch and validate user.
     */
    private UserDto fetchAndValidateUser(int userId, boolean validateEmail) {
        Response response = newRequest()
            .pathParam("id", userId)
            .when()
            .get("/users/{id}");
        
        if (response.getStatusCode() != 200) {
            throw new RuntimeException("Failed to fetch user: " + response.getStatusCode());
        }
        
        UserDto user = response.as(UserDto.class);
        
        if (validateEmail && (user.getEmail() == null || !user.getEmail().contains("@"))) {
            logger.warn("User ID: {} has invalid email: {}", userId, user.getEmail());
            throw new RuntimeException("Invalid email format for user: " + userId);
        }
        
        return user;
    }

    /**
     * Search users with pagination and sorting.
     * Uses imperative approach for complex query building.
     * 
     * @param searchTerm search term for name or username
     * @param page page number (0-based)
     * @param pageSize number of results per page
     * @param sortBy field to sort by
     * @param sortOrder sort order (asc/desc)
     * @return list of matching users
     */
    public List<UserDto> searchUsersWithPagination(String searchTerm, int page, int pageSize, 
                                                     String sortBy, String sortOrder) {
        logger.info("Searching users with pagination (imperative): term={}, page={}, size={}", 
                   searchTerm, page, pageSize);
        
        RequestSpecification request = newRequest();
        
        // Add search parameter if provided
        if (searchTerm != null && !searchTerm.isEmpty()) {
            request.queryParam("q", searchTerm);
        }
        
        // Add pagination parameters
        request.queryParam("_page", page)
               .queryParam("_limit", pageSize);
        
        // Add sorting parameters if provided
        if (sortBy != null && !sortBy.isEmpty()) {
            request.queryParam("_sort", sortBy);
            if (sortOrder != null && !sortOrder.isEmpty()) {
                request.queryParam("_order", sortOrder);
            }
        }
        
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
     * Execute a custom request with full control.
     * Demonstrates the ultimate flexibility of hybrid approach.
     * 
     * @param userId the user ID
     * @param customHeaders map of custom headers
     * @param logFullRequest whether to log full request/response
     * @return raw Response for custom processing
     */
    public Response executeCustomRequest(int userId, Map<String, String> customHeaders, 
                                         boolean logFullRequest) {
        logger.info("Executing custom request for user ID: {} (imperative)", userId);
        
        RequestSpecification request = getRequestSpecification()
            .pathParam("id", userId);
        
        // Add custom headers
        if (customHeaders != null && !customHeaders.isEmpty()) {
            customHeaders.forEach(request::header);
            logger.debug("Added custom headers: {}", customHeaders);
        }
        
        // Optional full logging
        if (logFullRequest) {
            request.log().all();
        }
        
        Response response = request
            .when()
            .get("/users/{id}");
        
        if (logFullRequest) {
            response.then().log().all();
        }
        
        return response;
    }
}