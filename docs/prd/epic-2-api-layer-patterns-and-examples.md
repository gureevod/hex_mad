# Epic 2: API Layer Patterns and Examples
**Epic Goal:** Standardize API models/services, error handling, and contract validation with pragmatic examples so teams can adopt Hex’s API layer independently and incrementally.

**Story 2.1: DTOs and Service Base Pattern**
As an API test developer,
I want a standardized DTO and service base pattern,
so that requests/responses are consistent and easy to extend.
**Acceptance Criteria:**
1. `hex-core-api` exposes `BaseService` with RestAssured setup, request spec builder, and response handling helpers.
2. DTO examples use Lombok (`@Value`, `@Builder`, `@With`) and Jackson annotations where needed.
3. Error model and exception mapping pattern documented (e.g., 4xx/5xx mapping).
4. Samples include at least two endpoints demonstrating GET/POST with typed responses.

**Story 2.2: Contract Validation Examples**
As a QA engineer,
I want schema/contract validation samples,
so that API behavior can be verified against contracts.
**Acceptance Criteria:**
1. Example JSON schema validation for responses (happy path and error cases).
2. Clear guidance for organizing schemas and integrating with RestAssured filters.
3. Allure steps capture contract validation results.
4. Negative test example included (e.g., missing field) with clear failure messaging.

**Story 2.3: Configurable Authentication Patterns**
As a test developer,
I want pluggable auth patterns,
so that services can authenticate consistently across environments.
**Acceptance Criteria:**
1. Owner-backed credentials/tokens via Vault retrieval pattern documented.
2. Request spec auth decorators (e.g., bearer token) with per-thread snapshot usage.
3. Example of rotating token provider with caching per test thread.
4. Logging redacts tokens; Allure shows non-sensitive auth steps only.

**Story 2.4: Service Test Scaffolds and Data Providers**
As a developer,
I want test scaffolds and JavaFaker-powered data providers,
so that I can write robust tests fast.
**Acceptance Criteria:**
1. Base test class for API services with lifecycle and tagging.
2. JavaFaker utilities for generating realistic payload examples.
3. Sample test suite structure and naming conventions.
4. Parallel-safe data creation practices documented.

**Story 2.5: Error Handling and Retries Guidance**
As a framework maintainer,
I want standardized retry and error handling guidance,
so that flaky external dependencies are mitigated.
**Acceptance Criteria:**
1. Optional retry policies documented (idempotent operations only).
2. Timeout and backoff recommendations documented per endpoint criticality.
3. Error translation patterns into domain-specific exceptions.
4. Examples demonstrate assertion of error responses and headers.

**Story 2.6: DTO Generation Workflow with jsonschema2pojo**
As a new adopter,
I want a simple workflow to generate DTOs from JSON,
so that I can quickly create models for service responses.
**Acceptance Criteria:**
1. Maven plugin configuration for jsonschema2pojo is provided with a pinned version.
2. Presets are configured to integrate with Lombok (`@Value`, `@Builder`) and Jackson annotations.
3. Conventions for generated package layout are documented in Russian.
4. An example shows copying a service JSON response, running the generator, and using the DTO in a test.
