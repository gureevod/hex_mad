# Epic 1: Core Framework Foundation and Parallel-Ready Scaffolding
**Epic Goal:** Deliver the initial `hex-core` modules and parallel-ready scaffolding so teams can run both API and UI sample tests with Allure in ≤ 60 minutes. Establish configuration via Owner, a simple factory pattern, logging via SLF4J/Logback with redaction, reporting via Allure, and JUnit 5 parallel execution with thread-safe defaults.

**Story 1.1: Bootstrap `hex-core` Modules and Project Structure**
As a framework maintainer,
I want a multi-module Maven structure with a base `hex-core` and specialized `hex-core-api`, `hex-core-ui`, and `hex-core-testing` modules,
so that teams can import focused modules and common abstractions are reused.
**Acceptance Criteria:**
1. A Maven parent POM defines modules `hex-core`, `hex-core-api`, `hex-core-ui`, `hex-core-testing`, and a sample `hex-project-samples`.
2. `hex-core` contains common abstractions (factories, config); `hex-core-api/ui/testing` depend on `hex-core`.
3. Shared dependency management includes pinned versions for Java 17, RestAssured, Selenide, JUnit 5, Allure, Owner, SLF4J/Logback, Lombok, JavaFaker, and AssertJ.
4. Each module builds successfully locally and in CI.
5. Sample project depends on `hex-core-*` without test flakiness in a single-thread run.
6. All module READMEs and Javadocs are created in Russian.

**Story 1.2: Owner Configuration and Factory Pattern**
As a test developer,
I want Owner-backed configuration and a simple factory pattern,
so that object creation is clear and type-safe with minimal overhead.
**Acceptance Criteria:**
1. Owner interfaces support environment profiles (local, CI).
2. Factory classes are documented with clear creation methods; no static mutable singletons.
3. Example instantiation for API client and UI driver/session via factory methods.

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

