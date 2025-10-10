# Test Strategy and Standards

This section outlines the strategy for testing the Hex framework's own components (`hex-core-*`). This internal testing is crucial for ensuring the framework is robust, reliable, and bug-free.

### Testing Philosophy
- **Approach:** The framework will be developed using a Test-Driven Development (TDD) approach where feasible, especially for new utilities and business logic in wrappers. All code contributions to `hex-core-*` must be accompanied by corresponding unit tests.
- **Coverage Goals:** A minimum of **85% line coverage** is required for all `hex-core-*` modules. This will be enforced in the CI pipeline using a tool like JaCoCo.
- **Test Pyramid:** The testing will be heavily focused on unit tests, as the framework's primary role is to provide code-level abstractions.
    - **Unit Tests (90%):** Testing individual classes and methods in isolation.
    - **Integration Tests (10%):** Testing the integration between Hex components (e.g., does the `BaseTest` lifecycle correctly configure an `ApiService`?).
    - **E2E Tests (via `hex-project-samples`):** The sample project serves as the end-to-end test suite for the framework, proving that all modules work together correctly to test a live application.

### Test Types and Organization
- **Unit Tests:**
    - **Framework:** JUnit 5 with Mockito for creating mocks and stubs.
    - **File Convention:** `*Test.java` (e.g., `ButtonWrapperTest.java`).
    - **Location:** `src/test/java` within each core module.
    - **Mocking Library:** Mockito (`5.12.0`).
    - **Coverage Requirement:** 85% line coverage enforced by JaCoCo.
    - **AI Agent Requirements:**
        - Generate tests for all public methods.
        - Cover both happy paths and exceptional cases (e.g., invalid arguments).
        - Follow the AAA pattern (Arrange, Act, Assert).
        - Mock all external dependencies (e.g., mock a `SelenideElement` when testing a wrapper).
- **Integration Tests:**
    - **Scope:** To verify interactions between components within the Hex framework itself, not with an external application.
    - **Location:** Within the `src/test/java` directory, but using a specific JUnit tag (`@Tag("integration")`) to separate them from fast-running unit tests.
    - **Test Infrastructure:** No external infrastructure needed. These tests will run entirely in-memory.
- **End-to-End (E2E) Tests:**
    - **Framework:** The Hex framework itself, used within the `hex-project-samples` module.
    - **Scope:** To validate that the fully assembled framework can successfully test a simple, live web application (e.g., `reqres.in` for API, a simple demo HTML page for UI).
    - **Environment:** These tests will run in CI against a real browser (headless Chrome).
    - **Test Data:** Will use `JavaFaker` for dynamic data.

### Test Data Management
- **Strategy:** For unit and integration tests, test data will be created directly within the test methods to ensure test isolation and clarity. For E2E tests in the sample project, `JavaFaker` will be used.
- **Fixtures:** No complex fixture loading. Simplicity is key.
- **Cleanup:** Not required for unit/integration tests. For E2E tests, the browser session is torn down after each test, ensuring a clean state.

### Continuous Testing
- **CI Integration:** The Maven build (`mvn clean install`) will automatically run all unit and integration tests. A failure in any test will fail the entire CI build. The `hex-project-samples` tests will be run in a separate, dedicated stage in the `Jenkinsfile.groovy`.

