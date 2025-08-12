# High Level Architecture

### Technical Summary
The Hex framework is designed as a modular monorepo using Maven, promoting a clear separation of concerns between reusable core modules (`hex-core-*`) and project-specific implementations (`hex-project`). The architecture prioritizes thread-safety and parallel execution by default, leveraging immutable configuration snapshots and a lightweight provider/factory pattern for dependency injection to eliminate global state. It provides extensible base abstractions for both UI (Selenide wrappers, composite components) and API (RestAssured services, DTOs) testing, enabling teams to build maintainable and scalable test suites while significantly reducing boilerplate code.

### High Level Overview
The core architectural style is a **"monolith of modules"** within a single monorepo. This approach allows for tight integration and atomic versioning of the core framework components while maintaining logical separation. The PRD's choice of a **Monorepo** structure is foundational, managed by a Maven parent POM. This simplifies dependency management and ensures consistency across all modules.

The primary data flow for a test is as follows:
1.  A JUnit 5 test class is instantiated.
2.  A per-thread context is created, including an immutable configuration snapshot (via Owner) and instances of required services/drivers (via DI providers).
3.  The test interacts with either API service classes or UI PageObjects.
4.  API services use a configured RestAssured client to make calls, mapping responses to DTOs.
5.  UI PageObjects use Selenide wrappers and composite components to interact with the browser, with actions and assertions automatically logged to SLF4J and reported to Allure.
6.  Test results, logs, and Allure artifacts are generated.

### High Level Project Diagram
```mermaid
graph TD
    subgraph Monorepo (Maven)
        direction LR
        ParentPOM("Parent POM")

        subgraph Core Modules
            CoreAPI("hex-core-api")
            CoreUI("hex-core-ui")
            CoreTesting("hex-core-testing")
        end

        subgraph Sample Implementation
            SampleProject("hex-project-samples")
        end

        ParentPOM --> CoreAPI
        ParentPOM --> CoreUI
        ParentPOM --> CoreTesting
        ParentPOM --> SampleProject

        SampleProject -- depends on --> CoreAPI
        SampleProject -- depends on --> CoreUI
        SampleProject -- depends on --> CoreTesting
    end

    subgraph External Dependencies
        RestAssured("RestAssured")
        Selenide("Selenide")
        JUnit5("JUnit 5")
        Allure("Allure")
        Owner("Owner")
        SLF4J("SLF4J/Logback")
    end

    CoreAPI --> RestAssured
    CoreUI --> Selenide
    CoreTesting --> JUnit5
    CoreTesting --> Allure
    CoreTesting --> Owner
    CoreTesting --> SLF4J


### Architectural and Design Patterns
- **Modular Monorepo:** Using a Maven parent POM to manage shared dependencies, plugins, and build lifecycle for all `hex-*` modules. _Rationale:_ Simplifies versioning, ensures consistency, and allows for atomic commits across related modules.
- **Dependency Injection (Google Guice):** Uses Google Guice for lightweight dependency injection to supply dependencies like WebDriver sessions or API clients. _Rationale:_ Provides compile-time safe dependency injection with minimal configuration overhead, promoting test-time flexibility and explicit, thread-safe dependency management.
- **Wrapper/Decorator Pattern:** Used for creating extensible wrappers around `SelenideElement` (e.g., `Button`, `Input`). _Rationale:_ Adds behavior like logging, custom waits, and business-centric assertions to base elements without altering Selenide's core.
- **Composite Pattern:** The foundation for user-defined UI components that group multiple element wrappers into a single, reusable business-level object (e.g., a `LoginForm` component). _Rationale:_ Enables building complex, hierarchical UI structures that are managed as a single unit.
- **Page Object Model (POM):** A standard UI testing pattern that will be implemented using the framework's wrappers and composite components. _Rationale:_ Separates UI interaction logic from test logic, improving maintainability and readability.
- **Service Layer Pattern:** For the API testing layer, where `Service` classes encapsulate endpoint interactions, request building, and response parsing. _Rationale:_ Abstracts the details of HTTP communication (via RestAssured) and provides a clean, business-oriented API for tests.
- **Immutable Objects:** Configuration objects provided by Owner will be treated as immutable snapshots for each thread. _Rationale:_ This is critical for achieving thread-safety and preventing state corruption during parallel test execution.

