# Epic 2: API Layer Patterns and Examples
**Epic Goal:** Standardize API models/services with both declarative (Retrofit2-style) and imperative (RestAssured) approaches, providing pragmatic examples so teams can adopt Hex's API layer independently and incrementally.

## Story 2.1: Declarative API Interface Pattern
**As an API test developer,**
**I want a declarative interface pattern similar to Retrofit2,**
**so that I can define API contracts with minimal boilerplate code.**

**Acceptance Criteria:**
1. Core annotations (`@GET`, `@POST`, `@PUT`, `@DELETE`, `@Path`, `@Query`, `@Body`, `@Header`) are implemented in `hex-core-api`.
2. `ApiServiceFactory.createDeclarative()` creates proxy implementations from annotated interfaces.
3. Return types support both `Response` and typed DTOs with automatic deserialization.
4. Sample interface demonstrates CRUD operations with type-safe DTOs.
5. Allure reporting and logging work transparently with declarative calls.

## Story 2.2: Hybrid Service Pattern (Declarative + Imperative)
**As an API test developer,**
**I want to combine declarative and imperative approaches in a single service,**
**so that I can handle both simple and complex API scenarios efficiently.**

**Acceptance Criteria:**
1. Services can implement declarative interfaces while extending `BaseApiService`.
2. Declarative methods are auto-implemented via proxy delegation.
3. Complex custom methods can use `newRequest()` for full RestAssured control.
4. Example shows batch operations, GraphQL, or complex request building.
5. Both approaches share the same configuration, retry logic, and logging.

## Story 2.3: DTOs and Service Base Pattern
**As an API test developer,**
**I want standardized DTO patterns and service base classes,**
**so that requests/responses are consistent across declarative and imperative approaches.**

**Acceptance Criteria:**
1. `hex-core-api` exposes `BaseApiService` with RestAssured setup and helpers.
2. DTO examples use Lombok (`@Value`, `@Builder`, `@With`) and Jackson annotations.
3. DTOs work seamlessly with both declarative return types and RestAssured `.as(Class)`.
4. Error model and exception mapping patterns documented for both approaches.
5. Samples include GET/POST with typed responses in both styles.

## Story 2.4: Contract Validation Examples
**As a QA engineer,**
**I want schema/contract validation that works with both API approaches,**
**so that API behavior can be verified against contracts regardless of implementation style.**

**Acceptance Criteria:**
1. JSON schema validation works with declarative interface responses.
2. RestAssured filter chain integrates with proxy-based calls.
3. Contract validation can be applied via `@Validated` annotation or filters.
4. Allure steps capture validation for both declarative and imperative calls.
5. Negative test examples for both approaches with clear failure messaging.

## Story 2.5: Configurable Authentication Patterns
**As a test developer,**
**I want authentication that works transparently with both API styles,**
**so that services authenticate consistently across environments.**

**Acceptance Criteria:**
1. Authentication configured once in `ApiConfig` applies to both approaches.
2. `@Headers` annotation supports static auth headers in declarative interfaces.
3. Dynamic token providers work with proxy-based calls.
4. Bearer token injection via interceptors for declarative services.
5. Examples show OAuth2, API keys, and basic auth in both styles.

## Story 2.6: Advanced Declarative Features
**As a framework user,**
**I want advanced declarative features for common patterns,**
**so that I can handle retries, timeouts, and caching declaratively.**

**Acceptance Criteria:**
1. `@Retry(count=5, delay=2000)` annotation for method-level retry configuration.
2. `@Timeout(seconds=30)` for custom timeout per endpoint.
3. `@Cache(duration=60)` for response caching (optional, future enhancement).
4. `@FormUrlEncoded` and `@Multipart` for different content types.
5. Examples show file upload, form submission, and retry scenarios.

## Story 2.7: Service Test Scaffolds and Data Providers
**As a developer,**
**I want test scaffolds that work with both API styles,**
**so that I can write robust tests quickly regardless of approach.**

**Acceptance Criteria:**
1. Base test class supports both `ApiServiceFactory.create()` and `.createDeclarative()`.
2. JavaFaker utilities generate data for both DTO objects and Map<String, Object>.
3. Test examples show declarative interfaces with typed DTOs.
4. Test examples show imperative approach for complex scenarios.
5. Parallel-safe service creation for both patterns documented.

## Story 2.8: Error Handling and Response Validation
**As a framework maintainer,**
**I want consistent error handling across both API approaches,**
**so that failures are reported uniformly.**

**Acceptance Criteria:**
1. Declarative methods throw consistent exceptions for HTTP errors.
2. Response validation works via return type (exception on type mismatch).
3. `@ExpectedStatus(200)` annotation for declarative status validation.
4. Error translation patterns work for both approaches.
5. Examples show error assertions in both declarative and imperative styles.

## Story 2.9: DTO Generation Workflow
**As a new adopter,**
**I want to generate DTOs that work with both API approaches,**
**so that I can quickly create models for service responses.**

**Acceptance Criteria:**
1. Maven plugin configuration for jsonschema2pojo with Lombok integration.
2. Generated DTOs include Jackson annotations for JSON mapping.
3. DTOs work as return types in declarative interfaces.
4. DTOs work with RestAssured's `.as(DtoClass.class)` method.
5. Example workflow: JSON → DTO → use in both declarative and imperative tests.

## Story 2.10: Migration Guide from Pure RestAssured
**As a team with existing RestAssured tests,**
**I want a clear migration path to the declarative approach,**
**so that we can adopt it incrementally without breaking existing tests.**

**Acceptance Criteria:**
1. Step-by-step guide for extracting interfaces from existing services.
2. Examples of converting RestAssured chains to declarative methods.
3. Backward compatibility confirmed - existing tests continue working.
4. Performance comparison showing minimal overhead.
5. Decision matrix: when to use declarative vs. imperative vs. hybrid.

## Implementation Priority
1. **Phase 1 (Core):** Stories 2.1, 2.3, 2.9 - Basic declarative support with DTOs
2. **Phase 2 (Integration):** Stories 2.2, 2.5, 2.7 - Hybrid approach and auth
3. **Phase 3 (Advanced):** Stories 2.4, 2.6, 2.8 - Validation and advanced features
4. **Phase 4 (Adoption):** Story 2.10 - Migration guide and best practices

## Technical Notes
- The declarative approach uses Java dynamic proxies (no compile-time processing initially)
- Full RestAssured access remains available via `BaseApiService.newRequest()`
- All existing `BaseApiService` features (retry, logging, config) work with both approaches
- Teams can mix approaches within the same project or even the same service class