# Technical Assumptions

### Repository Structure: Monorepo
- Rationale: Simplifies shared module management (`hex-core-*` and sample `hex-project` implementations), enables atomic versioning, and eases cross-module refactors.

### Service Architecture
- Monolith of modules within a monorepo: `hex-core`, `hex-core-api`, `hex-core-ui`, `hex-core-testing`, plus sample `hex-project-*` modules.
- Clear separation of concerns: `hex-core` contains common, reusable abstractions; `hex-project` contains project-specific PageObjects, composites, services, and DTOs.

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

