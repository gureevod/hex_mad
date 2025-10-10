# Epic 2: API Layer Patterns and Examples
**Epic Goal:** Standardize API models/services with a clean declarative (Retrofit2-style) approach, providing pragmatic examples so teams can adopt Hex's API layer independently and incrementally.

## Story 2.1: Core Declarative API Framework
**As an API test developer,**
**I want a declarative interface pattern similar to Retrofit2,**
**so that I can define API contracts with minimal boilerplate code.**

**Acceptance Criteria:**
1. Core annotations (`@GET`, `@POST`, `@PUT`, `@DELETE`, `@Path`, `@Query`, `@Body`, `@Header`) are implemented in `hex-core-api`.
2. `ApiServiceFactory.create()` creates proxy implementations from annotated interfaces.
3. Return types support typed DTOs with automatic JSON deserialization.
4. Sample interface demonstrates CRUD operations with type-safe DTOs.
5. Allure reporting and logging work transparently with declarative calls.

## Story 2.2: Modular Component Architecture
**As a framework developer,**
**I want a modular component architecture for the API layer,**
**so that components can be easily extended and customized.**

**Acceptance Criteria:**
1. Separate modules for annotation processing, request execution, and response conversion.
2. Interceptor chain pattern for cross-cutting concerns (auth, logging, retry).
3. Each component has single responsibility and clear interfaces.
4. Components can be extended or replaced without affecting others.
5. Factory uses builder pattern for flexible configuration.

## Story 2.3: DTOs and Type-Safe Responses
**As an API test developer,**
**I want standardized DTO patterns with type-safe responses,**
**so that API contracts are enforced at compile time.**

**Acceptance Criteria:**
1. DTO examples use Lombok (`@Value`, `@Builder`, `@With`) and Jackson annotations.
2. Declarative interfaces return typed DTOs with automatic JSON deserialization.
3. Error model and exception mapping patterns documented.
4. Generic support for `List<T>`, `Optional<T>`, and custom wrapper types.
5. Samples include GET/POST with typed responses and error handling.

## Story 2.4: Contract Validation and Error Handling
**As a QA engineer,**
**I want schema/contract validation integrated with declarative APIs,**
**so that API behavior can be verified against contracts automatically.**

**Acceptance Criteria:**
1. `@ExpectedStatus` annotation for HTTP status validation.
2. JSON schema validation via interceptors or annotations.
3. Custom error handling with typed exception mapping.
4. Allure steps capture validation results automatically.
5. Negative test examples with clear failure messaging.

## Story 2.5: Authentication and Security Patterns
**As a test developer,**
**I want authentication that works seamlessly with declarative APIs,**
**so that services authenticate consistently across environments.**

**Acceptance Criteria:**
1. `@Headers` annotation supports static auth headers.
2. Dynamic authentication via interceptors (Bearer tokens, API keys).
3. OAuth2 flow support with token refresh capabilities.
4. Per-service and per-method authentication configuration.
5. Examples show different auth patterns with declarative interfaces.

## Story 2.6: Advanced Declarative Features
**As a framework user,**
**I want advanced declarative features for common patterns,**
**so that I can handle complex scenarios without custom code.**

**Acceptance Criteria:**
1. `@Retry(count=3, delay=1000)` annotation for method-level retry configuration.
2. `@Timeout(seconds=30)` for custom timeout per endpoint.
3. `@FormUrlEncoded` and `@Multipart` for different content types.
4. `@Headers` for static and dynamic header configuration.
5. Examples show file upload, form submission, and retry scenarios.

## Story 2.7: Extensible Interceptor System
**As a framework user,**
**I want to add custom logic via interceptors,**
**so that I can handle complex scenarios that aren't covered by annotations.**

**Acceptance Criteria:**
1. Interceptor interface for request/response modification.
2. Chain pattern allows multiple interceptors in sequence.
3. Built-in interceptors for logging, auth, retry, and metrics.
4. Custom interceptor examples for GraphQL, caching, and transformation.
5. Interceptors can be configured per service or globally.

## Story 2.8: Test Scaffolds and Data Generation
**As a developer,**
**I want test scaffolds optimized for declarative APIs,**
**so that I can write robust tests quickly.**

**Acceptance Criteria:**
1. Base test class with declarative service creation helpers.
2. JavaFaker utilities generate data for DTO objects.
3. Test examples show declarative interfaces with realistic data.
4. Parallel-safe service creation patterns documented.
5. Integration with existing Hex testing infrastructure.

## Story 2.9: DTO Generation from OpenAPI/JSON Schema
**As a new adopter,**
**I want to generate DTOs from API specifications,**
**so that I can quickly create type-safe models.**

**Acceptance Criteria:**
1. Maven plugin configuration for jsonschema2pojo with Lombok integration.
2. Generated DTOs work seamlessly with declarative interfaces.
3. OpenAPI 3.0 support for comprehensive DTO generation.
4. Example workflow: OpenAPI spec → DTOs → declarative interface.
5. Generated code follows Hex framework conventions.

## Story 2.10: Performance and Monitoring
**As a framework maintainer,**
**I want performance monitoring and optimization for declarative APIs,**
**so that the framework scales well in enterprise environments.**

**Acceptance Criteria:**
1. Minimal reflection overhead with method metadata caching.
2. Built-in metrics collection for request timing and success rates.
3. Memory-efficient proxy implementation.
4. Performance benchmarks vs. direct RestAssured usage.
5. Monitoring integration with existing observability tools.

## Implementation Priority
1. **Phase 1 (Foundation):** Stories 2.1, 2.2, 2.3 - Core declarative framework with modular architecture
2. **Phase 2 (Features):** Stories 2.4, 2.5, 2.6 - Validation, auth, and advanced declarative features
3. **Phase 3 (Extensions):** Stories 2.7, 2.8, 2.9 - Interceptors, testing, and DTO generation
4. **Phase 4 (Optimization):** Story 2.10 - Performance monitoring and enterprise readiness

## Technical Notes
- Pure declarative approach using Java dynamic proxies for MVP
- Modular architecture allows easy extension without breaking changes
- Imperative support can be added later via interceptor system if needed
- Focus on developer experience and type safety over feature completeness