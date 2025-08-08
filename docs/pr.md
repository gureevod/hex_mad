# Hex Product Requirements Document (PRD)

## Goals and Background Context

### Goals
- Reduce boilerplate for UI and API tests across projects by ≥40%.
- Enable time-to-first-test ≤ 60 minutes via templates and sane defaults.
- Provide thread-safe, parallel-ready execution with ≤ 2% flake rate.
- Standardize patterns for wrappers, composites, DTOs, and services to improve maintainability.
- Support incremental adoption paths (API → UI → full core) to lower migration risk.
- Deliver CI quickstart (Jenkinsfile) for consistent pipelines and artifact publishing.

### Background Context
Hex is a modular Java 17/Maven automation framework unifying UI (Selenide) and API (RestAssured) testing with JUnit 5, Allure, Owner, and SLF4J/Logback. It separates reusable `hex-core` abstractions from `hex-project` implementations, emphasizing SOLID, DI, and thread-safety by default. The framework addresses duplication of wrappers/utilities, onboarding friction, and inconsistent patterns, while enabling composite UI components and standardized API service/models. It targets both net-new projects and migrating teams, offering incremental paths and a Jenkins quickstart for faster adoption and reliability.

### Change Log
| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-01-XX | 1.1 | Aligned with architecture v1.0 | PM |
| 2025-01-XX | 1.0 | Initial PRD draft | PM |

## Requirements

### Functional Requirements (FR)
1. FR1: Provide `hex-core-api` base client (RestAssured) with Owner-backed configuration, Allure hooks, and SLF4J + Logback logging, supporting request/response builders and error handling.
2. FR2: Provide `hex-core-ui` base wrappers for Selenide elements (`Button`, `Input`, etc.) with extension points for waiting, assertions, and logging (via SLF4J), plus example Lombok usage in PageObjects where appropriate.
3. FR3: Enable user-defined composite components via base abstractions and patterns, embeddable in PageObjects; do not ship concrete composites in `hex-core`.
4. FR4: Provide standardized API service layer patterns (DTOs/models and service classes) with request/response schemas and error handling guidelines; leverage Lombok to reduce boilerplate.
5. FR5: Provide JUnit 5 parallel-ready test scaffolds (base test classes, tags, lifecycle) for both UI and API, including isolated per-thread contexts and example JavaFaker usage for test data.
6. FR6: Provide DI wiring pattern (providers/factories) avoiding global singletons; enable test-time wiring for services, components, and configs.
7. FR7: Provide Jenkins quickstart `Jenkinsfile.groovy` with parallel stages, environment matrix support, and Allure report publishing.
8. FR8: Provide templates/samples for API-only adoption and combined UI+API adoption; include minimal starter `hex-project` examples.
9. FR9: Provide Allure reporting standards and integration for both UI and API layers with consistent step logging.
10. FR10: Provide logging standards based on SLF4J API and Logback implementation with redaction patterns for secrets/PII.
11. FR11: Provide configuration standards (Owner) with profiles for local vs CI, immutable per-thread snapshots, and Vault secret retrieval pattern documentation.
12. FR12: Ensure thread-safe WebDriver/Selenide lifecycle with per-thread sessions and no shared mutable state.
13. FR13: Provide compatibility matrix for Java 17, Selenide, RestAssured, JUnit 5, Allure, Owner, SLF4J/Logback, Lombok, JavaFaker, and AssertJ.
14. FR14: Provide sample API contract validation examples (schemas).
15. FR15: Provide examples and guidance for composite component integration into PageObjects and business logic encapsulation.
16. FR16: Provide documentation and patterns for migrating teams to adopt API-only, then UI wrappers, then full core incrementally.
17. FR17: Provide curated examples demonstrating Lombok patterns (DTOs, builders) and JavaFaker-based data providers in tests.
18. FR18: Provide jsonschema2pojo-based DTO generation workflow with Lombok and Jackson presets; document CLI/Maven plugin usage and standard package layout.
19. FR19: Provide typed collection abstractions using `ElementsCollection` under the hood, with filtering/mapping/assertions, waits, and Allure steps.

### Non-Functional Requirements (NFR)
1. NFR1: Parallel execution stability with flake rate ≤ 2% across 500+ monthly parallel runs.
2. NFR2: Time-to-first-test (UI and API) ≤ 60 minutes from template scaffold on a standard dev machine.
3. NFR3: Boilerplate reduction ≥ 40% in wrappers/setup vs baseline per-project implementations.
4. NFR4: Thread-safety by default: no shared mutable state; per-thread contexts; immutable configuration snapshots.
5. NFR5: Security: No secrets in code or plain Jenkins vars; use local HashiCorp Vault; implement log redaction for sensitive values; avoid PII in reports.
6. NFR6: Observability: Unified Allure reporting and consistent logging with standardized categories; MTTD improvements tracked.
7. NFR7: Compatibility: Company-custom Chromium only (latest stable); Java 17; Maven builds on Linux/macOS/Windows.
8. NFR8: Maintainability: Clear separation `hex-core` vs `hex-project`; semantic versioning and upgrade guides for `hex-core`.
9. NFR9: Extensibility: Well-defined extension points for wrappers, components, services, and configuration without modifying `hex-core`.
10. NFR10: Documentation: Provide clear examples (3–5 at launch) and patterns; maintain version compatibility matrix.
11. NFR11: All code docs, inline comments, Javadoc, and all user-facing READMEs must be written in Russian.

## User Interface Design Goals

### Overall UX Vision
- Provide a clean, discoverable abstraction layer over Selenide with minimal ceremony, consistent naming, and predictable behavior.
- Encourage composition: small element wrappers plus user-defined composite components encapsulating business logic usable within PageObjects.
- Make failure states actionable: enriched messages, Allure steps, and logs that pinpoint locator, condition, and context.

### Key Interaction Paradigms
- Wrapper-first approach: `Button`, `Input`, `Checkbox`, `Select`, etc., exposing common actions with built-in waits and assertions.
- Composite components pattern: user-defined `Component` base enabling nested elements, local waits, domain methods, and reusability.
- PageObjects composed of wrappers and composites; discourage raw `SelenideElement` usage in tests.
- Structured assertions with clear, fluent APIs and Allure steps.

## Technical Assumptions

### Repository Structure: Monorepo
- Rationale: Simplifies shared module management (`hex-core-*` and sample `hex-project` implementations), enables atomic versioning, and eases cross-module refactors.

### Service Architecture
- Monolith of modules within a monorepo: `hex-core`, `hex-core-api`, `hex-core-ui`, `hex-core-testing`, plus sample `hex-project-*` modules.
- Clear separation of concerns: `hex-core` contains common, reusable abstractions (DI, config); `hex-project` contains project-specific PageObjects, composites, services, and DTOs.

### Testing Requirements: Full Testing Pyramid
- Unit tests for wrappers, utilities, and service layer helpers.
- Integration tests for API clients (with mocked endpoints) and UI wrappers against a minimal demo app.
- E2E smoke flows via Selenide against sample pages in CI.

### Additional Technical Assumptions and Requests
- Java 17, Maven; company-custom Chromium only; headless mode default in CI.
- SLF4J + Logback for logging with redaction filter patterns; Allure for reporting.
- Owner for configuration; immutable per-thread snapshots; Vault pattern for secrets.
- Lombok to reduce DTO and helper boilerplate; JavaFaker for test data in examples.
- JUnit 5 parallelism enabled by default; per-thread WebDriver/Selenide sessions; no shared mutable state.
- RestAssured for API; Selenide for UI; Jenkins quickstart via `Jenkinsfile.groovy`.
- Compatibility matrix maintained for Java 17, Selenide, RestAssured, JUnit 5, Allure, Owner, SLF4J/Logback, Lombok, JavaFaker, AssertJ, and jsonschema2pojo.

## Epic List
- Epic 1: Core Framework Foundation and Parallel-Ready Scaffolding — Establish `hex-core` modules, DI pattern, configuration, logging, reporting, and JUnit 5 parallel scaffolds; deliver first working API and UI sample tests with Allure.
- Epic 2: API Layer Patterns and Examples — Standardize DTOs, services, error handling, and contract validation; provide adoption-ready examples and JavaFaker-powered data providers.
- Epic 3: UI Wrappers and Composite Component Pattern — Provide extensible Selenide wrappers and base composite component abstractions; deliver sample `hex-project` usage demonstrating PageObject composition and assertions.
- Epic 4: CI/CD Quickstart and Observability — Ship `Jenkinsfile.groovy` with parallel matrix, Allure publish, and artifacts; define log redaction and Owner profiles; provide Vault retrieval pattern documentation.
- Epic 5: Migration Toolkit and Compatibility — Document incremental adoption paths; supply compatibility matrix and migration guidance from ad hoc frameworks to `hex-core` abstractions.

## Epic 1: Core Framework Foundation and Parallel-Ready Scaffolding
**Epic Goal:** Deliver the initial `hex-core` modules and parallel-ready scaffolding so teams can run both API and UI sample tests with Allure in ≤ 60 minutes. Establish DI patterns, configuration via Owner, logging via SLF4J/Logback with redaction, reporting via Allure, and JUnit 5 parallel execution with thread-safe defaults.

**Story 1.1: Bootstrap `hex-core` Modules and Project Structure**
As a framework maintainer,
I want a multi-module Maven structure with a base `hex-core` and specialized `hex-core-api`, `hex-core-ui`, and `hex-core-testing` modules,
so that teams can import focused modules and common abstractions are reused.
**Acceptance Criteria:**
1. A Maven parent POM defines modules `hex-core`, `hex-core-api`, `hex-core-ui`, `hex-core-testing`, and a sample `hex-project-samples`.
2. `hex-core` contains common abstractions (DI, config); `hex-core-api/ui/testing` depend on `hex-core`.
3. Shared dependency management includes pinned versions for Java 17, RestAssured, Selenide, JUnit 5, Allure, Owner, SLF4J/Logback, Lombok, JavaFaker, and AssertJ.
4. Each module builds successfully locally and in CI.
5. Sample project depends on `hex-core-*` without test flakiness in a single-thread run.
6. All module READMEs and Javadocs are created in Russian.

**Story 1.2: Owner Configuration and DI Pattern**
As a test developer,
I want Owner-backed configuration and a lightweight provider/factory DI pattern,
so that test-time wiring is clear and no global singletons are required.
**Acceptance Criteria:**
1. Owner interfaces support environment profiles (local, CI) and immutable per-thread snapshots.
2. DI pattern documented with provider/factory classes; no static mutable singletons.
3. Example wiring for API client and UI driver/session via providers.
4. Unit tests verify configuration snapshot immutability and per-thread scoping.

**Story 1.3: Logging and Reporting Baseline**
As a developer,
I want standardized logging (SLF4J + Logback) and Allure integration,
so that diagnostics and reports are consistent across API and UI tests.
**Acceptance Criteria:**
1. Logback configuration with pattern layout and redaction filter for secrets/PII.
2. Allure listeners/hooks installed for API and UI layers; steps visible in reports.
3. Logging facade usage enforced in core modules; no direct System.out.
4. Sample tests show logs and Allure steps for actions and assertions.

**Story 1.4: JUnit 5 Parallel Execution and Thread-Safe Defaults**
As a developer,
I want parallel-ready defaults with thread-safe contexts,
so that suites scale reliably with low flake rates.
**Acceptance Criteria:**
1. JUnit 5 parallel config enabled; tags and test lifecycle base class provided.
2. Per-thread WebDriver/Selenide sessions; no shared mutable state.
3. API request/response builders scoped to test thread.
4. Sample parallel run passes consistently on headless Chromium.

**Story 1.5: First Working Samples (API + UI)**
As a new adopter,
I want minimal API and UI sample tests,
so that I can verify setup and reporting end-to-end quickly.
**Acceptance Criteria:**
1. API sample test hits a public mock or stub using RestAssured; Allure steps present.
2. UI sample test interacts with a simple demo page (input + button); Allure steps present.
3. Both samples runnable locally and in CI; artifacts archived; Allure report published.
4. Time-to-first-test path documented (≤ 60 minutes) in Russian.

## Epic 2: API Layer Patterns and Examples
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

## Epic 3: UI Wrappers and Composite Component Pattern
**Epic Goal:** Deliver extensible Selenide wrappers and a composite component base so teams can implement business-centric components and compose PageObjects cleanly.

**Story 3.1: Wrapper Base and Core Elements**
As a UI test developer,
I want robust element wrappers,
so that common actions are reliable and expressive.
**Acceptance Criteria:**
1. Base `Element` with locator, waits, visibility, and common assertions with Allure steps.
2. Concrete wrappers: `Button`, `Input`, `Checkbox`, `Select`, extend base and add idiomatic actions.
3. SLF4J-based logging for actions; no direct `SelenideElement` exposure in tests.
4. Fluent assertion helpers with clear failure messages.

**Story 3.2: Composite Component Base**
As a framework user,
I want a base for composite components,
so that I can define business-level components once and reuse them.
**Acceptance Criteria:**
1. `Component` base supports nested elements, local waits, and domain methods.
2. Guidance on parametrized components and scoping (root locator).
3. Thread-safe design—no shared state; page/context passed explicitly as needed.
4. Allure step conventions for component actions.

**Story 3.3: PageObject Composition Patterns**
As a developer,
I want PageObject patterns that compose wrappers and composites,
so that tests are readable and maintainable.
**Acceptance Criteria:**
1. Sample `hex-project` PageObjects composed from wrappers and composites.
2. Clear guidance discouraging test-level raw locators.
3. Examples for synchronization patterns and common gotchas (stale elements).
4. Demonstrate assertions at component and page levels.

**Story 3.4: Assertions and Waiting Utilities**
As a tester,
I want consistent waiting and assertion utilities,
so that tests fail fast with actionable feedback.
**Acceptance Criteria:**
1. Utilities expose common conditions with timeouts and polling defaults.
2. Assertion helpers integrate with Allure and use informative messages.
3. Thread-safe configuration of timeouts per test.
4. Examples cover dynamic content (modals, toasts).

**Story 3.5: Sample Composites in `hex-project` (not in core)**
As a maintainer,
I want non-core sample composites,
so that teams see practical usage without polluting `hex-core`.
**Acceptance Criteria:**
1. Sample `Table`, `Navbar`, `Modal` in `hex-project` only.
2. Example tests demonstrate composition and business methods.
3. Docs explicitly state no concrete composites ship in `hex-core`.
4. CI runs UI examples headless.

**Story 3.6: Collection Abstractions over ElementsCollection**
As a UI developer,
I want to work with collections of elements easily,
so that I can filter, map, and assert on lists of components.
**Acceptance Criteria:**
1. A typed wrapper around `ElementsCollection` provides common operations (size, filter, find, text assertions).
2. The collection wrapper includes built-in waits and Allure step conventions.
3. The design is thread-safe with no shared mutable state.
4. Example usage is provided in `hex-project` with a Russian README.

## Epic 4: CI/CD Quickstart and Observability
**Epic Goal:** Provide a Jenkins pipeline and observability standards to accelerate adoption and ensure consistent reporting/logging across projects.

**Story 4.1: Jenkinsfile Quickstart**
As a team lead,
I want a ready-to-use `Jenkinsfile.groovy`,
so that teams can run tests in parallel with artifacts and Allure.
**Acceptance Criteria:**
1. A sample `Jenkinsfile.groovy` is provided in the `hex-project-samples` module.
2. It defines stages for checkout, build, test (matrix), and report publish.
3. Environment matrix support (e.g., tags or profiles) is demonstrated.
4. It archives test reports/logs and publishes the Allure report.
5. Example parameters and customization documentation are provided in Russian.

**Story 4.2: Logging and Redaction Standards**
As a security-conscious maintainer,
I want consistent logging with redaction,
so that sensitive data is never exposed.
**Acceptance Criteria:**
1. Logback appenders and pattern layout with masking filter.
2. Documented list of sensitive keys to redact.
3. Tests verify redaction filter behavior.
4. Guidance on log levels per module.

**Story 4.3: Allure Reporting Conventions**
As a QA lead,
I want consistent Allure conventions,
so that reports are navigable and comparable.
**Acceptance Criteria:**
1. Standardized step naming and tagging.
2. Examples of attachments/screenshots for UI failures.
3. Custom categories guidance (post-MVP compatible).
4. Documentation on interpreting reports and MTTD tracking.

**Story 4.4: Owner Profiles and Vault Pattern**
As a developer,
I want standardized configuration profiles and secrets handling,
so that local and CI setups are consistent and safe.
**Acceptance Criteria:**
1. Owner profiles for local and CI; immutable snapshots per thread.
2. Documentation and example for Vault retrieval pattern.
3. No secrets in code or plain Jenkins variables.
4. Sample tests demonstrating config switching.

## Epic 5: Migration Toolkit and Compatibility
**Epic Goal:** Reduce friction for existing teams by providing compatibility guidance, examples, and a maintained version matrix.

**Story 5.1: Compatibility Matrix and Versioning**
As a maintainer,
I want a compatibility matrix,
so that teams know supported versions across libraries.
**Acceptance Criteria:**
1. Matrix for Java 17, Selenide, RestAssured, JUnit 5, Allure, Owner, SLF4J/Logback, Lombok, JavaFaker, AssertJ, and jsonschema2pojo is created.
2. Semantic versioning and upgrade notes for `hex-core` modules are documented.
3. Example upgrade path notes for minor/patch bumps are provided.
4. The matrix is published in the repository documentation in Russian.

**Story 5.2: Migration Guides and Patterns**
As a migrating team,
I want guides to convert ad hoc wrappers/services,
so that I can adopt Hex incrementally.
**Acceptance Criteria:**
1. Patterns for mapping existing wrappers to Hex wrapper base.
2. Service refactoring guide to `BaseService` pattern.
3. API-first adoption walkthrough before UI.
4. CI migration notes using the Jenkinsfile quickstart.

**Story 5.3: Samples for Incremental Adoption**
As a team lead,
I want minimal samples,
so that we can adopt one layer at a time.
**Acceptance Criteria:**
1. `hex-samples-api-only` and `hex-samples-ui-api` modules are provided.
2. Clear READMEs in Russian on adding dependencies and running tests.
3. Allure/Logback/Owner configuration examples included.
4. Parallel run notes and troubleshooting.