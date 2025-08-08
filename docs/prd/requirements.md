# Requirements

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
