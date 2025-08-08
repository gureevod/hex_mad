# Goals and Background Context

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
