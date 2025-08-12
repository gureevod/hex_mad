# Tech Stack

This section serves as the single source of truth for all technology and dependency versions used within the Hex framework. All Maven POMs must align with these specifications to ensure consistency and prevent dependency conflicts.

### Cloud Infrastructure
- **Provider:** N/A (Self-Hosted/On-Premise). The framework itself is not deployed to a cloud provider.
- **Key Services:**
    - **CI/CD:** Jenkins (assumed, per PRD).
    - **Source Control:** Git (e.g., GitHub, GitLab, Bitbucket).
    - **Artifact Repository:** Maven-compatible repository (e.g., Nexus, Artifactory) for publishing `hex-core` artifacts.
    - **UI Test Execution Grid (Future):** Designed for compatibility with Selenoid/Moon.
- **Deployment Regions:** N/A.

### Technology Stack Table

| Category | Technology | Version | Purpose | Rationale |
| :--- | :--- | :--- | :--- | :--- |
| **Language** | Java | 17 | Primary development language | LTS version, specified in PRD. Modern, stable, and widely adopted. |
| **Build Tool** | Apache Maven | 3.9.6 | Dependency management & build lifecycle | Industry standard for Java projects, robust multi-module support. |
| **UI Testing** | Selenide | 7.3.2 | Core UI interaction library | Provides concise, stable, and powerful wrappers over Selenium WebDriver. |
| **API Testing** | RestAssured | 5.4.0 | Core API testing client | Fluent, BDD-style API for testing REST services. |
| **Test Runner** | JUnit 5 | 5.10.2 | Test framework and execution engine | Modern, extensible Java testing standard with excellent parallel execution support. |
| **Reporting** | Allure Framework | 2.27.0 | Test reporting and visualization | Provides rich, interactive reports with steps, attachments, and history. |
| **Configuration** | Owner | 1.0.12 | Externalizing configuration | Reduces boilerplate for properties files, supports type-safe configs and profiles. |
| **Logging API** | SLF4J API | 2.0.13 | Logging facade | Decouples framework from a specific logging implementation. |
| **Logging Impl** | Logback Classic | 1.5.6 | Logging implementation | Powerful, fast, and highly configurable logging backend for SLF4J. |
| **Boilerplate** | Project Lombok | 1.18.32 | Code generation library | Drastically reduces boilerplate for DTOs, builders, and models. |
| **Dependency Injection** | Google Guice | 7.0.0 | Lightweight dependency injection framework | Provides compile-time safe dependency injection with minimal configuration overhead. |
| **Test Data** | JavaFaker | 1.0.2 | Realistic test data generation | Simple API for creating fake data (names, addresses, etc.) for tests. |
| **DTO Generation**| jsonschema2pojo | 1.2.1 | DTO generation from JSON Schema | Automates creation of POJOs from API contracts, ensuring consistency. |
| **Assertions** | AssertJ | 3.25.3 | Fluent assertions library | Provides highly readable and expressive assertions for tests. |

