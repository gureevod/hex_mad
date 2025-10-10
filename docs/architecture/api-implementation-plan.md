# API Layer Implementation Plan - Declarative Approach

## Executive Summary
This document outlines the implementation plan for introducing a declarative API testing approach (similar to Retrofit2) into the Hex framework while maintaining full backward compatibility with the existing RestAssured-based implementation.

## Architecture Overview

### Pure Declarative Approach for MVP
Since the existing hex-core-api implementation was just a dummy example, we're replacing it entirely with a clean, declarative-first architecture.

### New Modular Design
- **Declarative Only**: Clean annotated interfaces (Retrofit2-style) for MVP
- **Modular Components**: Single responsibility modules that can be extended/replaced
- **Future Extensibility**: Architecture supports adding imperative support later via interceptors if needed

## Implementation Phases

### Phase 1: Core Declarative Framework (Week 1-2)
**Goal**: Establish pure declarative API foundation

**Deliverables**:
1. Annotation system in `com.company.hex.api.annotations`
   - HTTP methods: `@GET`, `@POST`, `@PUT`, `@DELETE`, `@PATCH`
   - Parameters: `@Path`, `@Query`, `@Body`, `@Header`, `@FormParam`
   - Configuration: `@ApiService`, `@Headers`, `@Retry`, `@ExpectedStatus`

2. Modular component architecture
   - `AnnotationProcessor` for parsing method annotations
   - `RequestExecutor` for HTTP execution via RestAssured
   - `ResponseConverter` for type-safe response conversion
   - `InterceptorChain` for cross-cutting concerns

3. Clean `ApiServiceFactory`
   - Simple `create(Class<T> serviceInterface)` method
   - Builder pattern for custom configuration
   - No legacy methods - clean slate approach

**Modular Structure**:
```
hex-core-api/
├── src/main/java/com/company/hex/api/
│   ├── annotations/          # API annotations
│   │   ├── http/            # HTTP method annotations
│   │   ├── param/           # Parameter annotations
│   │   └── config/          # Configuration annotations
│   ├── processor/            # Annotation processing
│   │   ├── AnnotationProcessor.java
│   │   ├── MethodHandler.java
│   │   └── ParameterHandler.java
│   ├── executor/             # Request execution
│   │   ├── RequestExecutor.java
│   │   ├── RestAssuredExecutor.java
│   │   └── RetryPolicy.java
│   ├── converter/            # Response conversion
│   │   ├── ResponseConverter.java
│   │   ├── JacksonConverter.java
│   │   └── ErrorHandler.java
│   ├── interceptor/          # Request/Response interceptors
│   │   ├── Interceptor.java
│   │   ├── InterceptorChain.java
│   │   ├── LoggingInterceptor.java
│   │   └── AuthInterceptor.java
│   ├── proxy/                # Dynamic proxy
│   │   ├── ProxyHandler.java
│   │   └── ProxyFactory.java
│   ├── service/              # Service factory
│   │   └── ApiServiceFactory.java
│   └── model/                # Core models
│       ├── RequestDefinition.java
│       └── ApiConfig.java
```

### Phase 2: Sample Implementation (Week 2-3)
**Goal**: Demonstrate declarative approach with real examples

**Deliverables**:
1. Complete declarative interface example
   ```java
   @ApiService(baseUrl = "${api.base.url}")
   public interface UserApi {
       @GET("/users/{id}")
       UserDto getUserById(@Path("id") int userId);
       
       @POST("/users")
       @Headers("Content-Type: application/json")
       UserDto createUser(@Body CreateUserRequest request);
   }
   ```

2. DTO examples with Lombok and Jackson
   ```java
   @Value
   @Builder
   public class UserDto {
       int id;
       String name;
       String email;
   }
   ```

3. Test examples showing declarative usage

**Files to Create/Update**:
```
hex-project-samples/
├── src/main/java/com/company/hex/project/api/
│   ├── services/
│   │   ├── UserApiDeclarative.java (new)
│   │   ├── UserApiHybrid.java (new)
│   │   └── UserApiService.java (keep existing)
│   └── dto/
│       ├── UserDto.java
│       ├── CreateUserRequest.java
│       └── UpdateUserRequest.java
```

### Phase 3: Advanced Declarative Features (Week 3-4)
**Goal**: Add enterprise-grade declarative features

**Deliverables**:
1. Advanced annotations: `@Retry`, `@Timeout`, `@ExpectedStatus`
2. Form and multipart support: `@FormUrlEncoded`, `@Multipart`
3. Authentication interceptors for Bearer tokens, API keys
4. Error handling with typed exceptions
5. Interceptor system for extensibility

### Phase 4: Documentation & Migration (Week 4-5)
**Goal**: Enable smooth adoption

**Deliverables**:
1. Migration guide from pure RestAssured
2. Best practices documentation
3. Decision matrix for approach selection
4. Performance benchmarks
5. Training materials

## Technical Implementation Details

### Modular Component Flow
```
Interface Method Call
    ↓
ProxyHandler.invoke()
    ↓
AnnotationProcessor → RequestDefinition
    ↓
InterceptorChain.proceed()
    ├── AuthInterceptor
    ├── LoggingInterceptor
    └── CustomInterceptors
    ↓
RequestExecutor.execute()
    ├── RestAssuredExecutor (default)
    └── CustomExecutor (extensible)
    ↓
Response
    ↓
ResponseConverter.convert()
    ├── JacksonConverter (JSON)
    └── CustomConverters (XML, etc.)
    ↓
Return typed result
```

### Annotation Processing Flow
1. Read method annotations (`@GET`, `@POST`, etc.)
2. Extract path from annotation value
3. Process parameter annotations (`@Path`, `@Query`, `@Body`)
4. Build RestAssured request specification
5. Execute request with retry logic
6. Convert response to method return type

### Type Conversion Matrix
| Return Type | Conversion Strategy |
|------------|-------------------|
| `Response` | Direct return |
| `T` (DTO) | `response.as(T.class)` |
| `List<T>` | `response.as(new TypeRef<List<T>>(){})` |
| `Optional<T>` | Wrap in Optional, empty on 404 |
| `CompletableFuture<T>` | Async execution (future) |
| `void` | Status validation only |

## Clean Implementation

### Complete Replacement
Since the existing implementation was a dummy example, we're replacing it entirely:
1. Remove existing `BaseApiService` class
2. Remove existing `ApiServiceFactory` implementation
3. Implement new modular architecture from scratch

### New Features
1. **Primary**: Declarative interface-based services
2. **Modular**: Pluggable components (executors, converters, interceptors)
3. **Extensible**: Easy to add custom handlers and processors
4. **Type-safe**: Compile-time checking with generics
5. **Flexible**: Direct RestAssured access when needed

## Implementation Approach

### Start Fresh
Since we're replacing the dummy implementation:
1. Design declarative-first API
2. Build modular components
3. Provide RestAssured escape hatch for complex cases

### Example Usage
**Primary** (Declarative):
```java
@ApiService(baseUrl = "${api.base.url}")
public interface UserApi {
    @GET("/users/{id}")
    UserDto getUser(@Path("id") int id);
    
    @POST("/users")
    @Headers("Content-Type: application/json")
    UserDto createUser(@Body UserDto user);
}

// Usage
UserApi api = ApiServiceFactory.create(UserApi.class);
UserDto user = api.getUser(123);
```

**Fallback** (Complex RestAssured):
```java
// For complex scenarios, use RequestExecutor directly
RequestExecutor executor = ApiServiceFactory.getExecutor();
Response response = executor.executeRaw(request ->
    request
        .multiPart("file", file)
        .formParam("metadata", complexObject)
        .filter(customFilter)
        .when()
        .post("/complex/upload")
);
```

## Success Metrics

### Technical Metrics
- ✅ Zero breaking changes to existing code
- ✅ < 5ms overhead per declarative call
- ✅ 100% feature parity with RestAssured
- ✅ Full Allure/logging integration

### Developer Experience Metrics
- 📉 50-70% reduction in boilerplate code
- 📈 Improved API contract clarity
- 🎯 Type-safe API calls
- 🚀 Faster test development

## Benefits of Modular Design

### Extensibility Points
1. **Custom Converters**: Add XML, Protobuf, CSV converters
2. **Custom Interceptors**: Add metrics, caching, circuit breakers
3. **Custom Executors**: Add GraphQL, gRPC, WebSocket support
4. **Custom Processors**: Add validation, transformation logic

### Single Responsibility
Each module has one clear purpose:
- **AnnotationProcessor**: Parse annotations → RequestDefinition
- **RequestExecutor**: Execute HTTP requests
- **ResponseConverter**: Convert responses to Java types
- **InterceptorChain**: Apply cross-cutting concerns
- **ProxyHandler**: Orchestrate the flow

## Decision Guidelines

### When to Use Declarative (MVP Focus)
- ✅ All standard REST operations (CRUD)
- ✅ Type-safe responses needed
- ✅ Clean, maintainable API contracts
- ✅ Standard authentication patterns
- ✅ Most common testing scenarios

### When to Extend with Interceptors
- ✅ Complex request transformation
- ✅ Custom authentication flows
- ✅ Non-standard protocols (GraphQL, etc.)
- ✅ Advanced caching or circuit breaker patterns

### Future: When to Add Imperative Support
- ⏳ If declarative + interceptors prove insufficient
- ⏳ For highly dynamic request building
- ⏳ When direct RestAssured access is absolutely needed

## Timeline Summary

| Week | Phase | Deliverables |
|------|-------|-------------|
| 1-2 | Core Infrastructure | Annotations, Proxy, Factory |
| 2-3 | Sample Implementations | Examples, DTOs, Tests |
| 3-4 | Advanced Features | Retry, Validation, Error Handling |
| 4-5 | Documentation | Guides, Migration, Training |

## Conclusion

This implementation plan provides a clean, focused path to declarative API testing:
- **KISS**: Pure declarative approach for MVP - no hybrid complexity
- **YAGNI**: Start with essential features, add complexity only when needed
- **DRY**: Modular components prevent code duplication
- **Clean Slate**: No legacy baggage from dummy implementation
- **Extensible**: Interceptor system allows future enhancements

The pure declarative approach ensures developers get a clean, type-safe API testing experience while maintaining the flexibility to extend via interceptors when needed.